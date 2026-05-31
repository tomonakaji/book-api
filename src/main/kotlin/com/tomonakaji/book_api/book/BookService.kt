package com.tomonakaji.book_api.book

import com.tomonakaji.book_api.book.dto.BookResponse
import com.tomonakaji.book_api.book.dto.CreateBookRequest
import com.tomonakaji.book_api.common.BusinessRuleException
import com.tomonakaji.jooq.tables.references.AUTHORS
import com.tomonakaji.jooq.tables.references.BOOKS
import com.tomonakaji.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val dsl: DSLContext,
) {
    @Transactional
    fun create(request: CreateBookRequest): BookResponse {
        val authorIds = request.authorIds.distinct()
        validateAuthorIds(request.authorIds, authorIds)
        validateAuthorsExist(authorIds)

        val createdBook = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, request.title.trim())
            .set(BOOKS.PRICE, request.price)
            .set(BOOKS.PUBLICATION_STATUS, request.publicationStatus.name)
            .returning(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.PUBLICATION_STATUS)
            .fetchOne()
            ?: throw IllegalStateException("Failed to create book")

        authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, createdBook.get(BOOKS.ID)!!)
                .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
                .execute()
        }

        return BookResponse(
            id = createdBook.get(BOOKS.ID)!!,
            title = createdBook.get(BOOKS.TITLE)!!,
            price = createdBook.get(BOOKS.PRICE)!!,
            authorIds = authorIds,
            publicationStatus = PublicationStatus.valueOf(createdBook.get(BOOKS.PUBLICATION_STATUS)!!),
        )
    }

    private fun validateAuthorIds(rawAuthorIds: List<Int>, distinctAuthorIds: List<Int>) {
        if (rawAuthorIds.size != distinctAuthorIds.size) {
            throw BusinessRuleException("authorIds must not contain duplicates")
        }
    }

    private fun validateAuthorsExist(authorIds: List<Int>) {
        val existingAuthorIds = dsl.select(AUTHORS.ID)
            .from(AUTHORS)
            .where(AUTHORS.ID.`in`(authorIds))
            .fetch(AUTHORS.ID)

        if (existingAuthorIds.size != authorIds.size) {
            val missingAuthorIds = authorIds - existingAuthorIds.toSet()
            throw BusinessRuleException("authors not found: ids=$missingAuthorIds")
        }
    }
}
