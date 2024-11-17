package com.example.crudd.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.util.Date


data class UserCreateRequest(
    var fullName: String,
    var username: String,
    var balance: BigDecimal

)

data class AddBalance(
    var balance: BigDecimal
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserResponse(
    var id: Long,
    var fullName: String,
    var username: String,
    var balance: BigDecimal
)


data class UserUpdateRequest(
    var fullName: String?,
    var username: String?
)

data class UserTransactionHistory(
    var id: Long,
    var amount: BigDecimal,
    var transactionDate: Date
)

data class ProductTransaction(
    var productId: Long,
    var count: Long
)

data class UserPurchaseHistory(
    var productName: String,
    var count: Long,
    var amount: BigDecimal,
    var totalAmount: BigDecimal,
    var purchaseDate: Date
)

//////////////////////////////////    Category ga oid dto classlar /////////////////////////////////
data class CategoryCreateRequest(
    var name: String,
    var orderNumber: Long,
    var description: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CategoryResponse(
    var id: Long,
    var name: String,
    var order: Long,
    var description: String
)

data class CategoryUpdateRequest(
    var name: String?,
    var order: Long?,
    var description: String?
)


//////////////////////////////////  Product ga oid dto classlar ///////////////////////////////////////

data class ProductCreateRequest(
    var name: String,
    var count: Long,
    var amount: BigDecimal,
    var categoryId: Long
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductResponse(
    var id: Long,
    var name: String,
    var count: Long,
    var categoryId: Long
)

data class ProductUpdateRequest(
    var name: String?,
    var count: Long?,
    var categoryId: Long?,
    var amount: BigDecimal?
)

data class ProductResponseHistory(
    var id: Long,
    var name: String,
    var count: Long,
    var amount: BigDecimal
)

data class TransactionRequest(
    var transactionId: Long
)