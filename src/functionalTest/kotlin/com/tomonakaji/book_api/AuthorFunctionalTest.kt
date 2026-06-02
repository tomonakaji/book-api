package com.tomonakaji.book_api

import com.tomonakaji.jooq.tables.references.AUTHORS
import com.tomonakaji.jooq.tables.references.BOOKS
import com.tomonakaji.jooq.tables.references.BOOK_AUTHORS
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

class AuthorFunctionalTest : FunctionalTestBase() {
    @Test
    fun `正常系_POST_v1_authors_著者を登録できる`() {
        mockMvc
            .perform(
                post("/v1/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Suzuki Taro",
                          "birthDate": "1990-06-19"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.name", equalTo("Suzuki Taro")))
            .andExpect(jsonPath("$.birthDate", equalTo("1990-06-19")))
    }

    @Test
    fun `正常系_POST_v1_authors_birthDateが今日なら登録できる`() {
        val today = LocalDate.now()

        mockMvc
            .perform(
                post("/v1/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Tanaka Jiro",
                          "birthDate": "$today"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("Tanaka Jiro")))
            .andExpect(jsonPath("$.birthDate", equalTo(today.toString())))
    }

    @Test
    fun `異常系_POST_v1_authors_birthDateが明日なら400になる`() {
        val tomorrow = LocalDate.now().plusDays(1)

        mockMvc
            .perform(
                post("/v1/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Tanaka Jiro",
                          "birthDate": "$tomorrow"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `正常系_PUT_v1_authors_id_著者を更新できる`() {
        val authorId = insertAuthor(name = "Suzuki Taro", birthDate = LocalDate.parse("1990-06-19"))

        mockMvc
            .perform(
                put("/v1/authors/$authorId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Tanaka Jiro",
                          "birthDate": "1991-07-20"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(authorId)))
            .andExpect(jsonPath("$.name", equalTo("Tanaka Jiro")))
            .andExpect(jsonPath("$.birthDate", equalTo("1991-07-20")))
    }

    @Test
    fun `異常系_PUT_v1_authors_id_存在しない著者IDは404になる`() {
        mockMvc
            .perform(
                put("/v1/authors/999999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Tanaka Jiro",
                          "birthDate": "1991-07-20"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `異常系_PUT_v1_authors_id_birthDateが明日なら400になる`() {
        val authorId = insertAuthor(name = "Suzuki Taro", birthDate = LocalDate.parse("1990-06-19"))
        val tomorrow = LocalDate.now().plusDays(1)

        mockMvc
            .perform(
                put("/v1/authors/$authorId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Tanaka Jiro",
                          "birthDate": "$tomorrow"
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `正常系_GET_v1_authors_id_books_著者に紐づく書籍一覧を取得できる`() {
        val authorId = insertAuthor(name = "Yamamoto Hanako", birthDate = LocalDate.parse("1992-08-21"))
        val coAuthorId = insertAuthor(name = "Sato Tomoko", birthDate = LocalDate.parse("1993-09-22"))
        val firstBookId = insertBook(title = "Momotaro", price = 800, publicationStatus = "PUBLISHED")
        val secondBookId = insertBook(title = "Kaguyahime", price = 700, publicationStatus = "UNPUBLISHED")
        insertBookAuthor(bookId = firstBookId, authorId = authorId)
        insertBookAuthor(bookId = firstBookId, authorId = coAuthorId)
        insertBookAuthor(bookId = secondBookId, authorId = authorId)

        mockMvc
            .perform(get("/v1/authors/$authorId/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", equalTo(2)))
            .andExpect(jsonPath("$[0].id", equalTo(firstBookId)))
            .andExpect(jsonPath("$[0].title", equalTo("Momotaro")))
            .andExpect(jsonPath("$[0].price", equalTo(800)))
            .andExpect(jsonPath("$[0].publicationStatus", equalTo("PUBLISHED")))
            .andExpect(jsonPath("$[0].authorIds", containsInAnyOrder(authorId, coAuthorId)))
            .andExpect(jsonPath("$[1].id", equalTo(secondBookId)))
            .andExpect(jsonPath("$[1].title", equalTo("Kaguyahime")))
            .andExpect(jsonPath("$[1].price", equalTo(700)))
            .andExpect(jsonPath("$[1].publicationStatus", equalTo("UNPUBLISHED")))
            .andExpect(jsonPath("$[1].authorIds", containsInAnyOrder(authorId)))
    }

    @Test
    fun `正常系_GET_v1_authors_id_books_書籍が0件なら空配列を返す`() {
        val authorId = insertAuthor(name = "Yamamoto Hanako", birthDate = LocalDate.parse("1992-08-21"))

        mockMvc
            .perform(get("/v1/authors/$authorId/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", equalTo(0)))
    }

    @Test
    fun `異常系_GET_v1_authors_id_books_存在しない著者IDは404になる`() {
        mockMvc
            .perform(get("/v1/authors/999999/books"))
            .andExpect(status().isNotFound)
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
