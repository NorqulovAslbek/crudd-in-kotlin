package com.example.crudd.dto

import com.example.crudd.entity.Category
import com.example.crudd.entity.Product
import com.example.crudd.entity.Users
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.Date


data class BaseMessage(val code: Int, val message: String?)

data class UserCreateRequest(
    @field:NotBlank(message = "Toʻliq ism boʻsh boʻlmasligi kerak!!")
    var fullName: String,
    @field:Size(min = 3, max = 20, message = "username min 3 va undan uzunroq bolishi kerak!")
    var username: String,
)

data class AddBalance(
    @field:DecimalMin(value = "0.0", inclusive = false, message = "balansni 0 dan katta son kiriting!")
    var balance: BigDecimal
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserResponse(
    var id: Long,
    var fullName: String,
    var username: String,
    var balance: BigDecimal
) {
    companion object {
        fun Users.toDto() = UserResponse(id!!, this.fullName, this.username, this.balance)
    }
}


data class UserUpdateRequest(
    @field:NotBlank(message = "Toʻliq ism boʻsh boʻlmasligi kerak!!")
    var fullName: String?,
    @field:Size(min = 3, max = 20, message = "fulname min 3 va undan uzunroq bolishi kerak!")
    var username: String?
)

data class UserTransactionHistory(
    var id: Long,
    var amount: BigDecimal,
    var transactionDate: Date
)

data class ProductTransaction(
    var productId: Long,
    @field:Positive(message = "product counti manfiy bo'la olmaydi!")
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
    @field:Positive(message = "order number 0 dan kichik bola olmaydi!")
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
    @field:Positive(message = "order number 0 dan kichik bola olmaydi!")
    var order: Long?,
    var description: String?
)


//////////////////////////////////  Product ga oid dto classlar ///////////////////////////////////////

data class ProductCreateRequest(
    var name: String,
    @field:Positive(message = "product counti manfiy bo'la olmaydi!")
    var count: Long,
    @field:DecimalMin(value = "0.0", inclusive = false, message = "product narxi manfiy bola olmidi!")
    var amount: BigDecimal,
    var categoryId: Long
) {
    fun toEntity(category: Category) = Product(name, count, amount, category)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductResponse(
    var id: Long,
    var name: String,
    var count: Long,
    var categoryId: Long
)

data class ProductUpdateRequest(
    var name: String?,
    @field:PositiveOrZero(message = "product counti manfiy bo'la olmaydi!")
    var count: Long?,
    var categoryId: Long?,
    @field:DecimalMin(value = "0.0", inclusive = false, message = "product narxi manfiy bola olmidi!")
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