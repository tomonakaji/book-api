package com.tomonakaji.book_api

import com.tomonakaji.jooq.tables.references.AUTHORS
import com.tomonakaji.jooq.tables.references.BOOKS
import com.tomonakaji.jooq.tables.references.BOOK_AUTHORS
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

class BookFunctionalTest : FunctionalTestBase() {
    @Test
    fun `正常系_POST_v1_books_書籍を登録できる`() {
        val firstAuthorId = insertAuthor(name = "Suzuki Taro", birthDate = LocalDate.parse("1990-06-19"))
        val secondAuthorId = insertAuthor(name = "Tanaka Jiro", birthDate = LocalDate.parse("1991-07-20"))

        mockMvc
            .perform(
                post("/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "title": "Momotaro",
                          "price": 800,
                          "authorIds": [$firstAuthorId, $secondAuthorId],
                          "publicationStatus": "PUBLISHED"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.title", equalTo("Momotaro")))
            .andExpect(jsonPath("$.price", equalTo(800)))
            .andExpect(jsonPath("$.publicationStatus", equalTo("PUBLISHED")))
            .andExpect(jsonPath("$.authorIds", containsInAnyOrder(firstAuthorId, secondAuthorId)))
    }

    @Test
    fun `異常系_POST_v1_books_authorIdsに重複があると400になる`() {
        val authorId = insertAuthor(name = "Suzuki Taro", birthDate = LocalDate.parse("1990-06-19"))

        mockMvc
            .perform(
                post("/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "title": "Momotaro",
                          "price": 800,
                          "authorIds": [$authorId, $authorId],
                          "publicationStatus": "PUBLISHED"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `異常系_POST_v1_books_存在しない著者IDが含まれると400になる`() {
        val authorId = insertAuthor(name = "Suzuki Taro", birthDate = LocalDate.parse("1990-06-19"))

        mockMvc
            .perform(
                post("/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "title": "Momotaro",
                          "price": 800,
                          "authorIds": [$authorId, 999999],
                          "publicationStatus": "PUBLISHED"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `正常系_PUT_v1_books_id_書籍を更新できる`() {
        val firstAuthorId = insertAuthor(name = "Suzuki Taro", birthDate = LocalDate.parse("1990-06-19"))
        val secondAuthorId = insertAuthor(name = "Tanaka Jiro", birthDate = LocalDate.parse("1991-07-20"))
        val thirdAuthorId = insertAuthor(name = "Yamamoto Hanako", birthDate = LocalDate.parse("1992-08-21"))
        val bookId = insertBook(title = "Momotaro", price = 800, publicationStatus = "UNPUBLISHED")
        insertBookAuthor(bookId = bookId, authorId = firstAuthorId)
        insertBookAuthor(bookId = bookId, authorId = secondAuthorId)

        mockMvc
            .perform(
                put("/v1/books/$bookId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "title": "Kaguyahime",
                          "price": 900,
                          "authorIds": [$thirdAuthorId],
                          "publicationStatus": "PUBLISHED"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(bookId)))
            .andExpect(jsonPath("$.title", equalTo("Kaguyahime")))
            .andExpect(jsonPath("$.price", equalTo(900)))
            .andExpect(jsonPath("$.publicationStatus", equalTo("PUBLISHED")))
            .andExpect(jsonPath("$.authorIds", containsInAnyOrder(thirdAuthorId)))
    }

    @Test
    fun `異常系_PUT_v1_books_id_PUBLISHEDからUNPUBLISHEDへの変更は400になる`() {
        val authorId = insertAuthor(name = "Suzuki Taro", birthDate = LocalDate.parse("1990-06-19"))
        val bookId = insertBook(title = "Momotaro", price = 800, publicationStatus = "PUBLISHED")
        insertBookAuthor(bookId = bookId, authorId = authorId)

        mockMvc
            .perform(
                put("/v1/books/$bookId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "title": "Momotaro",
                          "price": 800,
                          "authorIds": [$authorId],
                          "publicationStatus": "UNPUBLISHED"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `異常系_PUT_v1_books_id_存在しない書籍IDは404になる`() {
        val authorId = insertAuthor(name = "Suzuki Taro", birthDate = LocalDate.parse("1990-06-19"))

        mockMvc
            .perform(
                put("/v1/books/999999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "title": "Momotaro",
                          "price": 800,
                          "authorIds": [$authorId],
                          "publicationStatus": "PUBLISHED"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isNotFound)
    }

    private fun insertAuthor(
        name: String,
        birthDate: LocalDate,
    ): Int =
        dsl
            .insertInto(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .returning(AUTHORS.ID)
            .fetchOne()
            ?.get(AUTHORS.ID)
            ?: error("failed to insert author")

    private fun insertBook(
        title: String,
        price: Int,
        publicationStatus: String,
    ): Int =
        dsl
            .insertInto(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.PUBLICATION_STATUS, publicationStatus)
            .returning(BOOKS.ID)
            .fetchOne()
            ?.get(BOOKS.ID)
            ?: error("failed to insert book")

    private fun insertBookAuthor(
        bookId: Int,
        authorId: Int,
    ) {
        dsl
            .insertInto(BOOK_AUTHORS)
            .set(BOOK_AUTHORS.BOOK_ID, bookId)
            .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
            .execute()
    }
}
