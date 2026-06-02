package com.tomonakaji.book_api.book

import com.tomonakaji.book_api.book.dto.BookResponse
import com.tomonakaji.book_api.book.dto.CreateBookRequest
import com.tomonakaji.book_api.book.dto.UpdateBookRequest
import com.tomonakaji.book_api.common.BusinessRuleException
import com.tomonakaji.book_api.common.NotFoundException
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

        val createdBook =
            dsl
                .insertInto(BOOKS)
                .set(BOOKS.TITLE, request.title.trim())
                .set(BOOKS.PRICE, request.price)
                .set(BOOKS.PUBLICATION_STATUS, request.publicationStatus.name)
                .returning(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.PUBLICATION_STATUS)
                .fetchOne() ?: throw IllegalStateException("failed to create book")

        authorIds.forEach { authorId ->
            dsl
                .insertInto(BOOK_AUTHORS)
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

    @Transactional
    fun update(
        bookId: Int,
        request: UpdateBookRequest,
    ): BookResponse {
        val authorIds = request.authorIds.distinct()
        validateAuthorIds(request.authorIds, authorIds)
        validateAuthorsExist(authorIds)

        val currentBook =
            dsl
                .select(BOOKS.PUBLICATION_STATUS)
                .from(BOOKS)
                .where(BOOKS.ID.eq(bookId))
                .fetchOne() ?: throw NotFoundException("book not found: $bookId")

        validatePublicationStatusTransition(
            currentStatus = PublicationStatus.valueOf(currentBook.get(BOOKS.PUBLICATION_STATUS)!!),
            nextStatus = request.publicationStatus,
        )

        val updatedBook =
            dsl
                .update(BOOKS)
                .set(BOOKS.TITLE, request.title.trim())
                .set(BOOKS.PRICE, request.price)
                .set(BOOKS.PUBLICATION_STATUS, request.publicationStatus.name)
                .where(BOOKS.ID.eq(bookId))
                .returning(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.PUBLICATION_STATUS)
                .fetchOne() ?: throw IllegalStateException("failed to update book")

        dsl
            .deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .execute()

        authorIds.forEach { authorId ->
            dsl
                .insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, bookId)
                .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
                .execute()
        }

        return BookResponse(
            id = updatedBook.get(BOOKS.ID)!!,
            title = updatedBook.get(BOOKS.TITLE)!!,
            price = updatedBook.get(BOOKS.PRICE)!!,
            authorIds = authorIds,
            publicationStatus = PublicationStatus.valueOf(updatedBook.get(BOOKS.PUBLICATION_STATUS)!!),
        )
    }

    private fun validateAuthorIds(
        rawAuthorIds: List<Int>,
        distinctAuthorIds: List<Int>,
    ) {
        if (rawAuthorIds.size != distinctAuthorIds.size) {
            throw BusinessRuleException("duplicate authorIds")
        }
    }

    private fun validateAuthorsExist(authorIds: List<Int>) {
        val existingAuthorIds =
            dsl
                .select(AUTHORS.ID)
                .from(AUTHORS)
                .where(AUTHORS.ID.`in`(authorIds))
                .fetch(AUTHORS.ID)

        if (existingAuthorIds.size != authorIds.size) {
            val missingAuthorIds = authorIds - existingAuthorIds.toSet()
            throw BusinessRuleException("authors not found: $missingAuthorIds")
        }
    }

    private fun validatePublicationStatusTransition(
        currentStatus: PublicationStatus,
        nextStatus: PublicationStatus,
    ) {
        if (currentStatus == PublicationStatus.PUBLISHED && nextStatus == PublicationStatus.UNPUBLISHED) {
            throw BusinessRuleException("invalid publication status transition")
        }
    }
}
