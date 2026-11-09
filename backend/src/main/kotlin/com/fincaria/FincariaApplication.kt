package com.fincaria

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.Locale

@SpringBootApplication
class FincariaApplication

fun main(args: Array<String>) {
    // Hibernate Validator interpolates its built-in messages against the JVM
    // default locale, independently of Spring's own LocaleContextHolder. Pin
    // it to English so API responses are consistent regardless of where the
    // app is deployed.
    Locale.setDefault(Locale.ENGLISH)
    runApplication<FincariaApplication>(*args)
}
