package com.tomonakaji.book_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BookApiApplication

fun main(args: Array<String>) {
    runApplication<BookApiApplication>(*args)
}
