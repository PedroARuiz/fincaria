package com.fincaria

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FincariaApplication

fun main(args: Array<String>) {
    runApplication<FincariaApplication>(*args)
}
