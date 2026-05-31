package com.tomonakaji.book_api.book.dto

import com.tomonakaji.book_api.book.PublicationStatus

data class BookResponse(
    val id: Int,
    val title: String,
    val price: Int,
    val authorIds: List<Int>,
    val publicationStatus: PublicationStatus,
)
