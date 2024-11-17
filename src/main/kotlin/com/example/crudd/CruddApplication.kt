package com.example.crudd

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing // Auditing funksiyasini yoqish
class CruddApplication

fun main(args: Array<String>) {
    runApplication<CruddApplication>(*args)
}
