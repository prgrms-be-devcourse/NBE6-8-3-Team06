//package com.back
//
//import org.springframework.boot.SpringApplication
//import org.springframework.boot.autoconfigure.SpringBootApplication
//import org.springframework.data.jpa.repository.config.EnableJpaAuditing
//
//@EnableJpaAuditing
//@SpringBootApplication
//object BackendApplication {
//    @JvmStatic
//    fun main(args: Array<String>) {
//        SpringApplication.run(BackendApplication::class.java, *args)
//    }
//}

package com.back

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class BackApplication

fun main(args: Array<String>) {
    runApplication<BackApplication>(*args)
}
