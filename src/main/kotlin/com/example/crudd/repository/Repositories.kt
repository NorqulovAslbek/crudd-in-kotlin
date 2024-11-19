package com.example.crudd.repository

import com.example.crudd.entity.*
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): List<T>
    fun findAllNotDeletedForPageable(pageable: Pageable): Page<T>
    fun saveAndRefresh(t: T): T
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    private val entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllNotDeleted(pageable: Pageable): List<T> = findAll(isNotDeletedSpecification, pageable).content
    override fun findAllNotDeletedForPageable(pageable: Pageable): Page<T> =
        findAll(isNotDeletedSpecification, pageable)

    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }

    @Transactional
    override fun saveAndRefresh(t: T): T {
        return save(t).apply { entityManager.refresh(this) }
    }
}


@Repository
interface UserRepository : BaseRepository<Users> {
    fun findByUsernameAndDeletedFalse(userName: String): Users?
}

@Repository
interface CategoryRepository : BaseRepository<Category>,PagingAndSortingRepository<Category,Long> {
    fun findByNameAndDeletedFalse(name: String): Category?
}


@Repository
interface ProductRepository : BaseRepository<Product> {

}

@Repository
interface UserPaymentTransactionRepository : BaseRepository<UserPaymentTransaction> {
    @Query("FROM UserPaymentTransaction AS u WHERE u.user.id=:userId AND u.deleted=false")
    fun findAllByUserIdAndDeletedFalse(@Param("userId") userId: Long): List<UserPaymentTransaction>
}


@Repository
interface TransactionRepository : BaseRepository<Transaction> {

}


@Repository
interface TransactionItemRepository : BaseRepository<TransactionItem> {

    @Query(
        """
        SELECT ti FROM TransactionItem AS ti
        INNER JOIN Transaction AS t
        ON ti.transaction.id=t.id
        WHERE ti.deleted=false
    """
    )
    fun findByUserId(userId: Long): List<TransactionItem>

    @Query(
        """
        SELECT p FROM Product p 
        INNER JOIN TransactionItem ti ON p.id=ti.product.id
        WHERE ti.transaction.id=:transactionId
    """
    )
    fun getByTransactionIdProduct(@Param("transactionId") transactionId: Long):List<Product>
}




