package com.company.app.common.crud

import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

abstract class BaseController<T : BaseEntity, D>(
    private val service: BaseService<T, D>
) {
    @GetMapping
    fun findAll(@PageableDefault(size = 20) pageable: Pageable) = service.findAll(pageable)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long) = service.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: D) = service.create(dto)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody dto: D) = service.update(id, dto)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = service.delete(id)
}
