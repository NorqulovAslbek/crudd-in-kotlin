package com.example.crudd.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Min
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.util.*


/**
 * BaseEntity entitysi - bu barcha entity class lar uchun ozini ichida umumiy bo'lgan fieldlarni ozida jamlaydigan class
 * Bu class barcha classlar uchun super class hisoblani boshqa entity classlar bu classdan inherit qilib ishlatadi
 *
 * @param id bu barcha entitylar uchun id hisoblanadi.
 * @param createdDate barcha entitylar uchun database ga saqlangan vaqtni ifodalaydi.
 *
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP)
    var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP)
    var modifiedDate: Date? = null, //ozgartirilgan vaqt
    @CreatedBy
    var createdBy: Long? = null,
    @LastModifiedBy
    var lastModifiedBy: Long? = null,
    @Column(nullable = false)
    @ColumnDefault(value = "false")
    var deleted: Boolean = false
)


/**
 * Category entitisi - mahsulot kategoriyasini ifodalaydi.
 *
 * Ushbu class, kategoriyaning nomi, tartib raqami va ta'rifi kabi ma'lumotlarni saqlaydi.
 *
 * @param name Kategoriyaning nomi, unique va null bo'lmasligi kerak.
 * @param order Kategoriyaning tartib raqami, null bo'lmasligi kerak.
 * @param description Kategoriyaning description ni, null bo'lmasligi kerak.
 *
 * Bu class `BaseEntity`dan meros oladi, shuning uchun umumiy ma'lumotlar (masalan, ID va yaratish vaqti) ham mavjud bo'ladi.
 */
@Entity
class Category(
    @Column(nullable = false, unique = true)  // Kategoriyaning nomi unik va null bo'lmasligi kerak
    var name: String,

    @Column(nullable = false)  // Kategoriyaning tartib raqami null bo'lmasligi kerak
    var orderNumber: Long,

    @Column(nullable = false)  // Kategoriyaning ta'rifi null bo'lmasligi kerak
    var description: String
) : BaseEntity()


/**
 * Product entitisi - mahsulotni ifodalaydi.
 *
 * Ushbu class, mahsulotning nomi, soni va tegishli kategoriyasi kabi ma'lumotlarni saqlaydi.
 *
 * @param name Mahsulotning nomi, null bo'lmasligi kerak.
 * @param count Mahsulotning soni, null bo'lmasligi mumkin, lekin bu qiymat nol yoki manfiy bo'lmasligi kerak.
 * @param category Mahsulotning tegishli kategoriyasi, null bo'lmasligi kerak va bu bog'lanish `Category` entitisi bilan amalga oshiriladi.
 *
 * `Product` class `Category` entitisi bilan bog'lanadi va bu bog'lanish `@ManyToOne` annotatsiyasi orqali amalga oshiriladi.
 */
@Entity
class Product(
    @Column(nullable = false)  // Mahsulotning nomi null bo'lmasligi kerak
    var name: String,

    @Column(nullable = false)
    @Min(0) // Mahsulotning soni, null bo'lmasligi mumkin, lekin bu qiymat nol yoki manfiy bo'lmasligi kerak.
    var count: Long,
    @Min(0)
    @Column(nullable = false)
    var amount: BigDecimal,
    @ManyToOne
    @JoinColumn(
        name = "category_id",
        nullable = false
    )
    var category: Category
) : BaseEntity()


/**
 * TransactionItem - Bu jadval tranzaksiyada sotilgan mahsulotlar haqida ma'lumotlarni saqlaydi.
 * Har bir tranzaksiya bir nechta mahsulotni o'z ichiga olishi mumkin, va har bir mahsulotning miqdori,
 * narxi va jami summasi bu jadvalda qayd etiladi.
 *
 * Ushbu jadval, asosan, quyidagi ma'lumotlarni o'z ichiga oladi:
 * @param  product - productId Mahsulotning o'ziga xos identifikatori (Product jadvaliga bog'lanadi).
 * @param count- Tranzaksiyada sotilgan mahsulot soni.
 * @param amount - Mahsulotning bir dona narxi.
 * @param totalAmount - Mahsulotning jami narxi (count * amount).
 * @param transaction - transactionId Tranzaksiyaning identifikatori (Transaction jadvaliga bog'lanadi).
 *
 * Bu jadval yordamida, bir tranzaksiyada sotilgan mahsulotlar va ularning narxlari haqida batafsil ma'lumot
 * olish imkoniyati yaratilib, ularni umumiy tranzaksiya bilan bog'lash osonlashadi.
 */
@Entity
class TransactionItem(
    @Column(nullable = false)// Ushbu mahsulotning tranzaksiyadagi miqdori
    @Min(1)
    var count: Long,
    @Column(nullable = false) // Mahsulotning bir dona uchun narxi (masalan, bir dona kitobning narxi)
    @Min(0)
    var amount: BigDecimal,
    @Column(nullable = false)// Ushbu mahsulotning jami narxi (count * amount)
    @Min(0)
    var totalAmount: BigDecimal,
    @ManyToOne
    @JoinColumn(
        name = "product_id",
        nullable = false
    )// Ushbu mahsulot tegishli bo'lgan tranzaksiya ID-si (Transaction jadvali bilan bog'langan)
    var product: Product,
    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    var transaction: Transaction
) : BaseEntity()


/**
 * transaction.http - bu entity class bizga transaction.http lar fielflaridan database dan jadval yasab beradi
 *
 * @param totalAmount bu transaction.http qanchaligi yani otkazilgan summani qanchaligi
 * @param user bu transactionni kim qilganini yani user id ni saqlanadi
 */
@Entity
class Transaction(
    @Column(nullable = false)
    @Min(0)
    var totalAmount: BigDecimal,
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: Users
) : BaseEntity()


/**
 * user-payment-transaction.http - bu class database da jadval yaratish uchun .
 * user ni tolovlari tarixi haqidagi barcha malumotlarni saqlash uchun ishlatiladi.
 * @param amount bu userni qancha summa otkazgani haqidagi malumot va bu not null bolishi kerak.
 * @param user bu qaysi user qancha otkazma qilganini aniqlash uchun ishlatiladigan table hisoblanadi.
 */
@Entity
class UserPaymentTransaction(
    @Column(nullable = false)
    @Min(0)
    var amount: BigDecimal,
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: Users
) : BaseEntity()


/**
 *  user entity - bu userlarni malumotlarini malumotlar bazasiga saqlash uchun ishlatiladi
 *
 *  @param fullName userning toliq ismi sharifini , null bolmasligi kerak
 *  @param username foydalanuvchi username bu field unique va null bomasligi kerak
 *  @param balance bu userning balanci bu fieldham not null bolishi kerak
 */
@Entity
class Users(
    @Column(nullable = false) //userning toliq ismi sharifini saqlidi lekn null bolishi mumkun
    var fullName: String,
    @Column(nullable = false, unique = true)// bu foydalanuvchining username bu field unique va not null
    var username: String,
    @Column(nullable = false) // bu userni balance bu filed null bomasligi kerak
    var balance: BigDecimal = BigDecimal(0)
) : BaseEntity()








