package com.example.crudd.controller

import com.example.crudd.dto.*
import com.example.crudd.exp.DemoExceptionHandler
import com.example.crudd.service.CategoryService
import com.example.crudd.service.ProductService
import com.example.crudd.service.TransactionService
import com.example.crudd.service.UserService
import jakarta.validation.Valid
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler(private val errorMessageSource: ResourceBundleMessageSource) {

    @ExceptionHandler(DemoExceptionHandler::class)
    fun handleAccountException(exception: DemoExceptionHandler): ResponseEntity<BaseMessage> {
        return ResponseEntity.badRequest().body(exception.getErrorMessage(errorMessageSource))
    }
}

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/add")
    fun create(@Valid @RequestBody userCreateRequest: UserCreateRequest) = userService.create(userCreateRequest)


    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long): UserResponse = userService.getOne(id)


    @PutMapping("/update/{id}")
    fun update(@PathVariable("id") id: Long, @Valid @RequestBody userUpdateRequest: UserUpdateRequest) =
        userService.update(id, userUpdateRequest)

    @DeleteMapping("/delete/{id}")
    fun delete(@PathVariable("id") id: Long) = userService.delete(id)

    @GetMapping("/pagination")
    fun getAllPagination(@RequestParam("page") page: Int, @RequestParam("size") size: Int) =
        userService.getAll(PageRequest.of(page - 1, size))

    @PostMapping("/{userId}/add-balance")
    fun addBalance(@Valid @RequestBody addBalance: AddBalance, @PathVariable userId: String) =
        userService.addBalance(userId.toLong(), addBalance.balance)

    @GetMapping("/transaction/{userId}")
    fun getTransactionByUserId(@PathVariable("userId") id: Long) = userService.getUserByIdTransaction(id)

}


@RestController
@RequestMapping("/category")
class CategoryController(
    private val categoryService: CategoryService
) {
    @PostMapping()
    fun create(@Valid @RequestBody createRequest: CategoryCreateRequest) = categoryService.create(createRequest)

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long) = categoryService.getOne(id)

    @PutMapping("/update/{id}")
    fun update(@PathVariable("id") id: Long, @Valid @RequestBody categoryUpdateRequest: CategoryUpdateRequest) =
        categoryService.update(id, categoryUpdateRequest)

    @DeleteMapping("/delete/{id}")
    fun delete(@PathVariable("id") id: Long) = categoryService.delete(id)

    @GetMapping("/pagenation")
    fun getAll(@RequestParam("page") page: Int, @RequestParam("size") size: Int) =
        categoryService.getAll(PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC,"orderNumber")))
}


@RestController
@RequestMapping("/product")
class Product(
    private val productService: ProductService
) {
    @PostMapping
    fun create(@Valid @RequestBody productCreateRequest: ProductCreateRequest) =
        productService.create(productCreateRequest)

    @PutMapping("/update/{id}")
    fun update(@PathVariable("id") id: Long, @Valid @RequestBody updateRequest: ProductUpdateRequest) =
        productService.update(id, updateRequest)

    @DeleteMapping("/delete/{id}")
    fun delete(@PathVariable("id") id: Long) = productService.delete(id)

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long) = productService.getOne(id)

    @GetMapping("/pagination")
    fun getAllPagination(@RequestParam("page") page: Int, @RequestParam("size") size: Int) =
        productService.getAll(PageRequest.of(page - 1, size))

}


@RestController
@RequestMapping("/transaction")
class Transaction(
    private val transactionService: TransactionService
) {
    @PostMapping("/{userId}")
    fun purchaseProduct(
        @PathVariable("userId") userId: Long,
        @Valid @RequestBody productTransaction: ProductTransaction
    ) =
        transactionService.purchaseProduct(userId, productTransaction.productId, productTransaction.count)

    @GetMapping("/history/{userId}")
    fun getUserPurchaseHistory(@PathVariable("userId") userId: Long) = transactionService.getUserPurchaseHistory(userId)

    @GetMapping("")
    fun getPurchasedProductsByUserTransaction(@RequestBody transactionRequest: TransactionRequest) =
        transactionService.getPurchasedProductsByUserTransaction(transactionRequest.transactionId)
}