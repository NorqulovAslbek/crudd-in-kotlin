package com.example.crudd.enums
enum class ErrorCodes(val code: Int) {
    USER_NOT_FOUND(100),
    USER_ALREADY_EXISTS(101),
    PRODUCT_NOT_FOUND(200),
    CATEGORY_NOT_FOUND(300),
    CATEGORY_ALREADY_EXISTS(301),
    USER_PURCHASE_HISTORY_NOT_FOUND(400),
    INSUFFICIENT_BALANCE(401),// balans yetmagan hatolig
    PRODUCT_QUANTITY_EXCEEDED(402) // Mahsulot miqdori oshgan xatolik

}
enum class UserRole {
    USER, ADMIN
}