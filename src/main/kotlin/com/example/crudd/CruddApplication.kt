package com.example.crudd

import com.example.crudd.repository.BaseRepositoryImpl
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl::class)
@EnableJpaAuditing // Auditing funksiyasini yoqish
class CruddApplication

fun main(args: Array<String>) {
    runApplication<CruddApplication>(*args)
}
