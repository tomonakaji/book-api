package com.tomonakaji.book_api.author

import com.tomonakaji.book_api.author.dto.AuthorResponse
import com.tomonakaji.book_api.author.dto.CreateAuthorRequest
import com.tomonakaji.book_api.author.dto.UpdateAuthorRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/authors")
class AuthorController(
    private val authorService: AuthorService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateAuthorRequest): AuthorResponse =
        authorService.create(request)

    @PutMapping("/{authorId}")
    fun update(
        @PathVariable authorId: Int,
        @Valid @RequestBody request: UpdateAuthorRequest,
    ): AuthorResponse = authorService.update(authorId, request)
}
