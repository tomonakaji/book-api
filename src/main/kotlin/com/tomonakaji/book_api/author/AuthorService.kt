package com.tomonakaji.book_api.author

import com.tomonakaji.book_api.author.dto.AuthorResponse
import com.tomonakaji.book_api.author.dto.CreateAuthorRequest
import com.tomonakaji.jooq.tables.references.AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class AuthorService(
    private val dsl: DSLContext,
) {
    fun create(request: CreateAuthorRequest): AuthorResponse {
        val createdAuthor = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, request.name.trim())
            .set(AUTHORS.BIRTH_DATE, request.birthDate)
            .returning(AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
            .fetchOne()
            ?: throw IllegalStateException("Failed to create author")

        return AuthorResponse(
            id = createdAuthor.get(AUTHORS.ID)!!,
            name = createdAuthor.get(AUTHORS.NAME)!!,
            birthDate = createdAuthor.get(AUTHORS.BIRTH_DATE)!!,
        )
    }
}
