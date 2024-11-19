package com.example.crudd.exp

import com.example.crudd.dto.BaseMessage
import com.example.crudd.enums.ErrorCodes
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource


sealed class DemoExceptionHandler : RuntimeException() {
    abstract fun errorCode(): ErrorCodes
    open fun getAllArguments(): Array<Any?>? = null

    fun getErrorMessage(resourceBundle: ResourceBundleMessageSource): BaseMessage {
        val message = try {
            resourceBundle.getMessage(  // USER_NOT_FOUND
                errorCode().name, getAllArguments(), LocaleContextHolder.getLocale()
            )
        } catch (e: Exception) {
            e.message
        }
        return BaseMessage(errorCode().code, message)
    }
}


class UserAlreadyExistsException : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.USER_ALREADY_EXISTS
}

class UserNotFoundExistsException : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.USER_NOT_FOUND
}

class ProductNotFoundExistsException : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.PRODUCT_NOT_FOUND
}

class CategoryNotFoundExistException : DemoExceptionHandler() {
    override fun errorCode(): ErrorCodes = ErrorCodes.CATEGORY_NOT_FOUND
}

class CategoryAlreadyExistsException : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.CATEGORY_ALREADY_EXISTS
}

class UserPurchaseHistoryNotFoundException : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.USER_PURCHASE_HISTORY_NOT_FOUND
}

class InsufficientBalance : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.INSUFFICIENT_BALANCE
}

class ProductQuantityExceeded : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.PRODUCT_QUANTITY_EXCEEDED
}
