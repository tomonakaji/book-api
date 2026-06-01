package com.tomonakaji.book_api.book.dto

import com.tomonakaji.book_api.book.PublicationStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.PositiveOrZero

data class UpdateBookRequest(
    @field:NotBlank
    val title: String,
    @field:PositiveOrZero
    val price: Int,
    @field:NotEmpty
    val authorIds: List<Int>,
    val publicationStatus: PublicationStatus,
)
