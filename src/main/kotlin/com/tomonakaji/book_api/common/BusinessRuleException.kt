package com.tomonakaji.book_api.common

class BusinessRuleException(
    override val message: String,
) : RuntimeException(message)
