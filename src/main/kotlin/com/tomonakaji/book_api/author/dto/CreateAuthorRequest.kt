package com.tomonakaji.book_api.author.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate

data class CreateAuthorRequest(
    @field:NotBlank
    val name: String,
    @field:PastOrPresent
    val birthDate: LocalDate,
)
