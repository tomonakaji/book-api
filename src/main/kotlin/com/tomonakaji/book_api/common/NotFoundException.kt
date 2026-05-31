package com.tomonakaji.book_api.common

class NotFoundException(
    override val message: String,
) : RuntimeException(message)
