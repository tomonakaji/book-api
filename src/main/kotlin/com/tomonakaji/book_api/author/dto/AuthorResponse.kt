package com.tomonakaji.book_api.author.dto

import java.time.LocalDate

data class AuthorResponse(
    val id: Int,
    val name: String,
    val birthDate: LocalDate,
)
