package com.tomonakaji.book_api.book

import com.tomonakaji.book_api.common.BusinessRuleException
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class BookRulesTest {
    @Test
    fun `正常系_authorIdsに重複がなければ例外にならない`() {
        BookRules.validateAuthorIds(listOf(1, 2, 3))
    }

    @Test
    fun `異常系_authorIdsに重複があると例外になる`() {
        assertFailsWith<BusinessRuleException> {
            BookRules.validateAuthorIds(listOf(1, 1, 2))
        }
    }

    @Test
    fun `正常系_未出版から出版済みへの変更は許可される`() {
        BookRules.validatePublicationStatusTransition(
            currentStatus = PublicationStatus.UNPUBLISHED,
            nextStatus = PublicationStatus.PUBLISHED,
        )
    }

    @Test
    fun `正常系_未出版から未出版への変更は許可される`() {
        BookRules.validatePublicationStatusTransition(
            currentStatus = PublicationStatus.UNPUBLISHED,
            nextStatus = PublicationStatus.UNPUBLISHED,
        )
    }

    @Test
    fun `正常系_出版済みから出版済みへの変更は許可される`() {
        BookRules.validatePublicationStatusTransition(
            currentStatus = PublicationStatus.PUBLISHED,
            nextStatus = PublicationStatus.PUBLISHED,
        )
    }

    @Test
    fun `異常系_出版済みから未出版への変更は例外になる`() {
        assertFailsWith<BusinessRuleException> {
            BookRules.validatePublicationStatusTransition(
                currentStatus = PublicationStatus.PUBLISHED,
                nextStatus = PublicationStatus.UNPUBLISHED,
            )
        }
    }
}
