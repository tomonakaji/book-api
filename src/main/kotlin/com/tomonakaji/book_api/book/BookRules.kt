package com.tomonakaji.book_api.book

import com.tomonakaji.book_api.common.BusinessRuleException

object BookRules {
    fun validateAuthorIds(authorIds: List<Int>) {
        if (authorIds.size != authorIds.distinct().size) {
            throw BusinessRuleException("duplicate authorIds")
        }
    }

    fun validatePublicationStatusTransition(
        currentStatus: PublicationStatus,
        nextStatus: PublicationStatus,
    ) {
        if (currentStatus == PublicationStatus.PUBLISHED && nextStatus == PublicationStatus.UNPUBLISHED) {
            throw BusinessRuleException("invalid publication status transition")
        }
    }
}
