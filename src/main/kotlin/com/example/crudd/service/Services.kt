package com.example.crudd.service

import com.example.crudd.dto.*
import com.example.crudd.entity.*
import com.example.crudd.exp.AppBadException
import com.example.crudd.repository.*
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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
        if (existingUser != null) throw AppBadException("bunaqa username li user allaqachon mavjud!!")
        if (request.balance.toDouble() < 0) throw AppBadException("user balanci manfiy bola olmaydi!")
        val user = Users(request.fullName, request.username, request.balance)
        userRepository.save(user)
    }

    @Transactional
    override fun update(id: Long, request: UserUpdateRequest) {
        val userEntity = userRepository.findByIdAndDeletedFalse(id)
            ?: throw AppBadException("bunday  id lik ysni $id teng user mavjud emas!")
        request.username?.let { userEntity.username = it }
        request.fullName?.let { userEntity.fullName = it }
        userRepository.save(userEntity)
    }


    @Transactional
    override fun delete(id: Long) {
        val option = userRepository.findById(id)
        if (!option.isEmpty) {
            userRepository.deleteById(id)
            return
        }
        throw AppBadException("bunaqa id li id=$id user mavjud emas")
    }

    override fun getOne(id: Long): UserResponse {
        val userEntity = userRepository.findByIdAndDeletedFalse(id)
            ?: throw AppBadException("bunaqa id li id=$id user mavjud emas!")
        return UserResponse(userEntity.id!!, userEntity.fullName, userEntity.username, userEntity.balance)
    }

    override fun getAll(pageable: Pageable): Page<UserResponse> {
        val allUsers = userRepository.findAllByDeletedFalse(pageable)
        val userList = allUsers.content.map { users ->
            UserResponse(
                id = users.id!!,
                fullName = users.fullName,
                username = users.username,
                balance = users.balance
            )
        }
        return PageImpl(userList, pageable, allUsers.totalElements)
    }


    /**
     * user oz akkauntini balamsini toldirish jarayoni.
     */

    override fun addBalance(id: Long, balance: BigDecimal) {
        val user =
            userRepository.findByIdAndDeletedFalse(id)
                ?: throw AppBadException("bunaqa id=${id} lik user topilmadi!")
        user.balance += balance
        userRepository.save(user)
        val userEntityById = entityManager.getReference(Users::class.java, id)
        val userTransaction = UserPaymentTransaction(balance, userEntityById)
        userPaymentTransactionRepository.save(userTransaction)
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
    override fun purchaseProduct(userId: Long, productId: Long, count: Long) {
        val user = userRepository.findByIdAndDeletedFalse(userId)
            ?: throw AppBadException("Foydalanuvchi id=$userId topilmadi!")
        val product = productRepository.findByIdAndDeletedFalse(productId)
            ?: throw AppBadException("product id=$productId topilmadi!")
        if (product.count < count) throw AppBadException(
            "Faqatgina ${product.count} dona mahsulot mavjud, lekin siz $count dona so'radingiz!"
        )
        val totalAmount = product.amount * BigDecimal(count)
        if (totalAmount > user.balance) throw AppBadException("${totalAmount - user.balance} summa yetarli emas, Hisobingizni toldiring!!")

        user.balance -= totalAmount
        product.count -= count
        userRepository.save(user)

        val transaction = Transaction(
            user = user,
            totalAmount = totalAmount
        )
        transactionRepository.save(transaction)

        val transactionItem = TransactionItem(
            product = product,
            count = count,
            amount = product.amount,
            totalAmount = totalAmount,
            transaction = transaction
        )
        transactionItemRepository.save(transactionItem)
    }

    override fun getUserPurchaseHistory(userId: Long): List<UserPurchaseHistory> {
        val transactionHistory = transactionItemRepository.findByUserId(userId)
        if (transactionHistory.isEmpty()) throw AppBadException("foydalanuvchi mahsulotlar boyicha sotib olish tarixi topilmadi!")
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
            if (categoryEntity != null) throw AppBadException("bunday name=$name lik category mavjud!!")
        }
        categoryRepository.save(Category(request.name, request.orderNumber, request.description))
    }

    @Transactional
    override fun update(id: Long, request: CategoryUpdateRequest) {
        val categoryEntity =
            categoryRepository.findCategoryByIdAndDeletedFalse(id)
                ?: throw AppBadException("bunday id li id=$id category yoq")
        request.name?.let { categoryEntity.name = it }
        request.order?.let { categoryEntity.orderNumber = it }
        request.description?.let { categoryEntity.description = it }
        categoryRepository.save(categoryEntity)
    }

    @Transactional
    override fun delete(id: Long) {
        val optional = categoryRepository.findById(id)
        if (optional.isPresent) {
            categoryRepository.deleteById(id)
            return
        }
        throw AppBadException("bunday id=$id ga teng bolgan category yoq!!")
    }

    override fun getOne(id: Long): CategoryResponse {
        val category = categoryRepository.findCategoryByIdAndDeletedFalse(id)
            ?: throw AppBadException("bunaqa id lik id=$id category mavjudmas!")
        return CategoryResponse(category.id!!, category.name, category.orderNumber, category.description)
    }

    override fun getAll(pageable: Pageable): Page<CategoryResponse> {
        val categoryListEntity = categoryRepository.findAllByDeletedFalse(pageable)
        val categoryList = LinkedList<CategoryResponse>()
        categoryListEntity.content.map { category ->
            categoryList.add(
                CategoryResponse(
                    id = category.id!!,
                    name = category.name,
                    order = category.orderNumber,
                    description = category.description
                )
            )
        }
        return PageImpl(categoryList, pageable, categoryListEntity.totalElements)
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
        val category = categoryRepository.findById(request.categoryId).orElseThrow {
            AppBadException("bunday id=${request.categoryId} lik category mavjud emas!!")
        }
        if (request.count < 0) throw AppBadException("product count ti manfiy bo'la olmaydi!!")
        val product = Product(
            name = request.name,
            count = request.count,
            category = category,
            amount = request.amount
        )
        productRepository.save(product)
    }


    @Transactional
    override fun update(id: Long, request: ProductUpdateRequest) {
        val product = productRepository.findByIdAndDeletedFalse(id)
            ?: throw AppBadException("bunaqa id=$id lik product mavjud emas!!")
        if (request.count!! < 0) throw AppBadException("product count ti manfiy bo'la olmaydi!!")
        request.name?.let { product.name = it }
        request.count?.let { product.count = it }
        request.categoryId?.let { product.category.id = it }
        request.amount?.let { product.amount = it }

        productRepository.save(product)
    }

    @Transactional
    override fun delete(id: Long) {
        val optional = productRepository.findById(id)
        if (optional.isPresent) {
            productRepository.deleteById(id)
            return
        }
        throw AppBadException("bunday id=${id} lik product mavjud emas!!")
    }

    override fun getOne(id: Long): ProductResponse {
        return productRepository.findByIdAndDeletedFalse(id)?.let {
            ProductResponse(name = it.name, id = it.id!!, count = it.count, categoryId = it.category.id!!)
        } ?: throw AppBadException("bunday id=${id} lik product mavjud emas!!")

    }

    override fun getAll(pageable: Pageable): Page<ProductResponse> {
        val pageableAll = productRepository.findAllByDeletedFalse(pageable)
        val productList = LinkedList<ProductResponse>()
        pageableAll.content.map { product ->
            productList.add(
                ProductResponse(
                    id = product.id!!,
                    name = product.name,
                    count = product.count,
                    categoryId = product.category.id!!
                )
            )
        }
        return PageImpl(productList, pageable, pageableAll.totalElements)
    }

}

