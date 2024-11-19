package com.example.crudd.service

import com.example.crudd.dto.*
import com.example.crudd.dto.UserResponse.Companion.toDto
import com.example.crudd.entity.*
import com.example.crudd.exp.AppBadException
import com.example.crudd.repository.*
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

interface UserService {
    fun create(request: UserCreateRequest)
    fun update(id: Long, request: UserUpdateRequest)
    fun delete(id: Long)
    fun getOne(id: Long): UserResponse
    fun getAll(pageable: Pageable): Page<UserResponse>
    fun addBalance(id: Long, balance: BigDecimal)
    fun getUserByIdTransaction(userId: Long): List<UserTransactionHistory>

}

/**
 * bu user service bunda hamma userlar malumotlari ustida ammalar bajariladi create update delete va user sotib
 * olgan productlar transaction.http lar manashu service da ishlov beriladi.
 */
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userPaymentTransactionRepository: UserPaymentTransactionRepository,
    private val entityManager: EntityManager

) : UserService {
    override fun create(request: UserCreateRequest) {
        val existingUser = userRepository.findByUsernameAndDeletedFalse(request.username)
        if (existingUser != null) throw AppBadException("bunaqa user allaqachon bor")//throw UserAlreadyExistsException()
        val user = Users(request.fullName, request.username)
        userRepository.saveAndRefresh(user)
    }

    @Transactional
    override fun update(id: Long, request: UserUpdateRequest) {
        val userEntity = userRepository.findByIdAndDeletedFalse(id)
            ?: throw AppBadException("not found user")//throw UserNotFoundExistsException()
        if (request.username != null && userRepository.findByUsernameAndDeletedFalse(request.username!!) == null) {
            request.username?.let { userEntity.username = it }
        }
        request.fullName?.let { userEntity.fullName = it }
        userRepository.saveAndRefresh(userEntity)
    }


    @Transactional
    override fun delete(id: Long) {
        userRepository.trash(id) ?: throw AppBadException("user not found")
        //  throw UserNotFoundExistsException()
    }

    override fun getOne(id: Long): UserResponse {
        val userEntity = userRepository.findByIdAndDeletedFalse(id)
            ?: throw AppBadException("user not fount") //throw UserNotFoundExistsException()
        userEntity.toDto()
        return UserResponse(userEntity.id!!, userEntity.fullName, userEntity.username, userEntity.balance)
    }

    override fun getAll(pageable: Pageable): Page<UserResponse> {
        val allUsers = userRepository.findAllNotDeletedForPageable(pageable)
        return allUsers.map { users ->
            UserResponse(
                id = users.id!!,
                fullName = users.fullName,
                username = users.username,
                balance = users.balance
            )
        }
//        return PageImpl(userList, pageable, allUsers.totalElements)
    }


    /**
     * user oz akkauntini balamsini toldirish jarayoni.
     */

    override fun addBalance(id: Long, balance: BigDecimal) {
        val user =
            userRepository.findByIdAndDeletedFalse(id)
                ?: throw AppBadException("user not found") //throw UserNotFoundExistsException()
        user.balance += balance
        userRepository.save(user)
        val userEntityById = entityManager.getReference(Users::class.java, id)
        val userTransaction = UserPaymentTransaction(balance, userEntityById)
        userPaymentTransactionRepository.saveAndRefresh(userTransaction)
    }


    /**
     * user oz hisob raqamiga otkazgan pullar tarixini kora olishi kerak
     */

    override fun getUserByIdTransaction(userId: Long): List<UserTransactionHistory> {
        val users = userPaymentTransactionRepository.findAllByUserIdAndDeletedFalse(userId)
        val userTransactionList = LinkedList<UserTransactionHistory>()
        users.map { user ->
            userTransactionList.add(
                UserTransactionHistory(
                    id = user.id!!,
                    amount = user.amount,
                    transactionDate = user.createdDate!!
                )
            )
        }
        return userTransactionList
    }
}


interface TransactionService {
    fun purchaseProduct(userId: Long, productId: Long, count: Long)
    fun getUserPurchaseHistory(userId: Long): List<UserPurchaseHistory>

    fun getPurchasedProductsByUserTransaction(transactionId: Long): List<ProductResponseHistory>
}

@Service
class TransactionServiceImpl(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionItemRepository: TransactionItemRepository

) : TransactionService {
    @Transactional
    override fun purchaseProduct(userId: Long, productId: Long, count: Long) {
        val user = userRepository.findByIdAndDeletedFalse(userId)
            ?: throw AppBadException("not found user") //throw UserNotFoundExistsException()
        val product = productRepository.findByIdAndDeletedFalse(productId)
            ?: throw AppBadException("product not found") //throw ProductNotFoundExistsException()
        if (product.count < count) throw AppBadException("product soni yetarli emas")//throw ProductQuantityExceeded()
        val totalAmount = product.amount * BigDecimal(count)
        if (totalAmount > user.balance) throw AppBadException("palance yetarli emas")//throw InsufficientBalance()

        user.balance -= totalAmount
        product.count -= count
        userRepository.save(user)

        val transaction = Transaction(
            user = user,
            totalAmount = totalAmount
        )
        transactionRepository.saveAndRefresh(transaction)

        val transactionItem = TransactionItem(
            product = product,
            count = count,
            amount = product.amount,
            totalAmount = totalAmount,
            transaction = transaction
        )
        transactionItemRepository.saveAndRefresh(transactionItem)
    }

    @Transactional
    override fun getUserPurchaseHistory(userId: Long): List<UserPurchaseHistory> {
        val transactionHistory = transactionItemRepository.findByUserId(userId)
        if (transactionHistory.isEmpty()) throw AppBadException("userid si boyicha transactionlar topilmadi")//throw UserPurchaseHistoryNotFoundException()
        val purchaseHistoryList = LinkedList<UserPurchaseHistory>()
        transactionHistory.map { transactionItem ->
            purchaseHistoryList.add(
                UserPurchaseHistory(
                    productName = (productRepository.findByIdAndDeletedFalse(transactionItem.product.id!!)!!.name),
                    count = transactionItem.count,
                    amount = transactionItem.amount,
                    totalAmount = transactionItem.totalAmount,
                    purchaseDate = transactionItem.createdDate!!
                )
            )

        }
        return purchaseHistoryList
    }

    @Transactional
    override fun getPurchasedProductsByUserTransaction(transactionId: Long): List<ProductResponseHistory> {
        val products = transactionItemRepository.getByTransactionIdProduct(transactionId)
        val productList = LinkedList<ProductResponseHistory>()
        products.map { product ->
            productList.add(
                ProductResponseHistory(
                    id = product.id!!,
                    name = product.name,
                    amount = product.amount,
                    count = product.count
                )
            )
        }
        return productList;
    }
}


interface CategoryService {
    fun create(request: CategoryCreateRequest)
    fun update(id: Long, request: CategoryUpdateRequest)
    fun delete(id: Long)
    fun getOne(id: Long): CategoryResponse
    fun getAll(pageable: Pageable): Page<CategoryResponse>


}

@Service
class CategoryServiceImpl(
    private var categoryRepository: CategoryRepository
) : CategoryService {
    override fun create(request: CategoryCreateRequest) {
        request.run {
            val categoryEntity = categoryRepository.findByNameAndDeletedFalse(request.name)
            if (categoryEntity != null) throw AppBadException("category allaqachon bor")//throw CategoryAlreadyExistsException()
        }
        categoryRepository.saveAndRefresh(Category(request.name, request.orderNumber, request.description))
    }

    @Transactional
    override fun update(id: Long, request: CategoryUpdateRequest) {
        val categoryEntity =
            categoryRepository.findByIdAndDeletedFalse(id)
                ?: throw AppBadException("category topilmadi") //throw CategoryNotFoundExistException()
        request.name?.let { categoryEntity.name = it }
        request.order?.let { categoryEntity.orderNumber = it }
        request.description?.let { categoryEntity.description = it }
        categoryRepository.saveAndRefresh(categoryEntity)
    }

    @Transactional
    override fun delete(id: Long) {
        categoryRepository.trash(id) ?: throw AppBadException("category topilmadi")
//        throw CategoryNotFoundExistException()
    }

    override fun getOne(id: Long): CategoryResponse {
        val category = categoryRepository.findByIdAndDeletedFalse(id)
            ?: throw AppBadException("category topilmadi!")//throw CategoryNotFoundExistException()
        return CategoryResponse(category.id!!, category.name, category.orderNumber, category.description)
    }

    override fun getAll(pageable: Pageable): Page<CategoryResponse> {
        val categoryListEntity = categoryRepository.findAllNotDeletedForPageable(pageable)
        return categoryListEntity.map { category ->
            CategoryResponse(
                id = category.id!!, // Ensure `id` is not null
                name = category.name,
                order = category.orderNumber,
                description = category.description
            )
        }
    }

}


interface ProductService {
    fun create(request: ProductCreateRequest)
    fun update(id: Long, request: ProductUpdateRequest)
    fun delete(id: Long)
    fun getOne(id: Long): ProductResponse
    fun getAll(pageable: Pageable): Page<ProductResponse>

}

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
) : ProductService {
    override fun create(request: ProductCreateRequest) {
        val category = categoryRepository.findByIdAndDeletedFalse(request.categoryId)
            ?: throw AppBadException("category topilmadi")// CategoryNotFoundExistException()

        val product = Product(
            name = request.name,
            count = request.count,
            category = category,
            amount = request.amount
        )
//        val product = request.toEntity(category)
        productRepository.saveAndRefresh(product)
    }


    @Transactional
    override fun update(id: Long, request: ProductUpdateRequest) {
        val product = productRepository.findByIdAndDeletedFalse(id)
            ?: throw AppBadException("product topilmadi.") //throw ProductNotFoundExistsException()
        request.name?.let { product.name = it }
        request.count?.let { product.count = it }
        request.categoryId?.let { product.category.id = it }
        request.amount?.let { product.amount = it }

        productRepository.saveAndRefresh(product)
    }

    @Transactional
    override fun delete(id: Long) {
        productRepository.trash(id) ?: throw AppBadException("product topilmadi!")
        //  throw ProductNotFoundExistsException()
    }

    override fun getOne(id: Long): ProductResponse {
        return productRepository.findByIdAndDeletedFalse(id)?.let {
            ProductResponse(name = it.name, id = it.id!!, count = it.count, categoryId = it.category.id!!)
        } ?: throw AppBadException("product topilmadi")//throw ProductNotFoundExistsException()

    }

    override fun getAll(pageable: Pageable): Page<ProductResponse> {
        val pageableAll = productRepository.findAllNotDeletedForPageable(pageable)
        return pageableAll.map { product ->
            ProductResponse(
                id = product.id!!,
                name = product.name,
                count = product.count,
                categoryId = product.category.id!!
            )
        }
    }

}

