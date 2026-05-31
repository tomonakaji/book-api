package com.tomonakaji.book_api.author

import com.tomonakaji.book_api.author.dto.AuthorResponse
import com.tomonakaji.book_api.author.dto.CreateAuthorRequest
import com.tomonakaji.book_api.author.dto.UpdateAuthorRequest
import com.tomonakaji.book_api.common.NotFoundException
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

    fun update(authorId: Int, request: UpdateAuthorRequest): AuthorResponse {
        val updatedAuthor = dsl.update(AUTHORS)
            .set(AUTHORS.NAME, request.name.trim())
            .set(AUTHORS.BIRTH_DATE, request.birthDate)
            .where(AUTHORS.ID.eq(authorId))
            .returning(AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
            .fetchOne()
            ?: throw NotFoundException("author not found: id=$authorId")

        return AuthorResponse(
            id = updatedAuthor.get(AUTHORS.ID)!!,
            name = updatedAuthor.get(AUTHORS.NAME)!!,
            birthDate = updatedAuthor.get(AUTHORS.BIRTH_DATE)!!,
        )
    }
}
