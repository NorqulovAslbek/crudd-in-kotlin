package com.example.crudd.repository

import com.example.crudd.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface UserRepository : JpaRepository<Users, Long> {
    fun findByUsernameAndDeletedFalse(userName: String): Users?
    fun findByIdAndDeletedFalse(id: Long): Users?
    fun findAllByDeletedFalse(pageable: Pageable): Page<Users>
}

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findAllByDeletedFalse(pageable: Pageable): Page<Category>
    fun findCategoryByIdAndDeletedFalse(id: Long): Category?
    fun findByNameAndDeletedFalse(name: String): Category?
}


@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findAllByDeletedFalse(pageable: Pageable): Page<Product>
    fun findByIdAndDeletedFalse(id: Long): Product?

}

@Repository
interface UserPaymentTransactionRepository : JpaRepository<UserPaymentTransaction, Long> {
    @Query("FROM UserPaymentTransaction AS u WHERE u.user.id=:userId AND u.deleted=false")
    fun findAllByUserIdAndDeletedFalse(@Param("userId") userId: Long): List<UserPaymentTransaction>
}


@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {

}


@Repository
interface TransactionItemRepository : JpaRepository<TransactionItem, Long> {

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




