package com.tomonakaji.book_api

import com.tomonakaji.jooq.tables.references.AUTHORS
import com.tomonakaji.jooq.tables.references.BOOKS
import com.tomonakaji.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
abstract class FunctionalTestBase {
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var dsl: DSLContext

    @Autowired
    protected lateinit var webApplicationContext: WebApplicationContext

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        dsl.deleteFrom(BOOK_AUTHORS).execute()
        dsl.deleteFrom(BOOKS).execute()
        dsl.deleteFrom(AUTHORS).execute()
    }
}
