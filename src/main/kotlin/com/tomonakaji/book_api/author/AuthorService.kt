package com.tomonakaji.book_api.author

import com.tomonakaji.book_api.author.dto.AuthorResponse
import com.tomonakaji.book_api.author.dto.CreateAuthorRequest
import com.tomonakaji.book_api.author.dto.UpdateAuthorRequest
import com.tomonakaji.book_api.book.PublicationStatus
import com.tomonakaji.book_api.book.dto.BookResponse
import com.tomonakaji.book_api.common.NotFoundException
import com.tomonakaji.jooq.tables.references.AUTHORS
import com.tomonakaji.jooq.tables.references.BOOKS
import com.tomonakaji.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class AuthorService(
    private val dsl: DSLContext,
) {
    fun create(request: CreateAuthorRequest): AuthorResponse {
        val createdAuthor =
            dsl
                .insertInto(AUTHORS)
                .set(AUTHORS.NAME, request.name.trim())
                .set(AUTHORS.BIRTH_DATE, request.birthDate)
                .returning(AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
                .fetchOne() ?: throw IllegalStateException("failed to create author")

        return AuthorResponse(
            id = createdAuthor.get(AUTHORS.ID)!!,
            name = createdAuthor.get(AUTHORS.NAME)!!,
            birthDate = createdAuthor.get(AUTHORS.BIRTH_DATE)!!,
        )
    }

    fun update(
        authorId: Int,
        request: UpdateAuthorRequest,
    ): AuthorResponse {
        val updatedAuthor =
            dsl
                .update(AUTHORS)
                .set(AUTHORS.NAME, request.name.trim())
                .set(AUTHORS.BIRTH_DATE, request.birthDate)
                .where(AUTHORS.ID.eq(authorId))
                .returning(AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
                .fetchOne() ?: throw NotFoundException("author not found: $authorId")

        return AuthorResponse(
            id = updatedAuthor.get(AUTHORS.ID)!!,
            name = updatedAuthor.get(AUTHORS.NAME)!!,
            birthDate = updatedAuthor.get(AUTHORS.BIRTH_DATE)!!,
        )
    }

    fun findBooksByAuthor(authorId: Int): List<BookResponse> {
        ensureAuthorExists(authorId)

        return dsl
            .select(
                BOOKS.ID,
                BOOKS.TITLE,
                BOOKS.PRICE,
                BOOKS.PUBLICATION_STATUS,
                BOOK_AUTHORS.AUTHOR_ID,
            ).from(BOOKS)
            .join(BOOK_AUTHORS)
            .on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
            .where(
                BOOKS.ID.`in`(
                    dsl
                        .select(BOOK_AUTHORS.BOOK_ID)
                        .from(BOOK_AUTHORS)
                        .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId)),
                ),
            ).orderBy(BOOKS.ID.asc(), BOOK_AUTHORS.AUTHOR_ID.asc())
            .fetch()
            .groupBy { it.get(BOOKS.ID)!! }
            .values
            .map { records ->
                val firstRecord = records.first()
                BookResponse(
                    id = firstRecord.get(BOOKS.ID)!!,
                    title = firstRecord.get(BOOKS.TITLE)!!,
                    price = firstRecord.get(BOOKS.PRICE)!!,
                    authorIds = records.map { it.get(BOOK_AUTHORS.AUTHOR_ID)!! },
                    publicationStatus = PublicationStatus.valueOf(firstRecord.get(BOOKS.PUBLICATION_STATUS)!!),
                )
            }
    }

    private fun ensureAuthorExists(authorId: Int) {
        val authorExists =
            dsl.fetchExists(
                dsl
                    .selectOne()
                    .from(AUTHORS)
                    .where(AUTHORS.ID.eq(authorId)),
            )

        if (!authorExists) {
            throw NotFoundException("author not found: $authorId")
        }
    }
}
