package org.springframework.samples.petclinic

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

object PetClinicApplication : SpringBootServletInitializer {
    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(PetClinicApplication::class.java, args)
    }
}
