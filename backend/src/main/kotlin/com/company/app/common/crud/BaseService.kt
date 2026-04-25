package com.company.app.common.crud

import com.company.app.common.exception.AppException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

abstract class BaseService<T : BaseEntity, D>(
    private val repository: BaseRepository<T, Long>
) {
    abstract fun toEntity(dto: D): T
    abstract fun updateEntity(entity: T, dto: D)

    open fun findAll(pageable: Pageable): Page<T> = repository.findAllActive(pageable)

    open fun findById(id: Long): T =
        repository.findActiveById(id) ?: throw AppException.notFound()

    open fun create(dto: D): T = repository.save(toEntity(dto))

    open fun update(id: Long, dto: D): T {
        val entity = findById(id)
        updateEntity(entity, dto)
        entity.updatedAt = LocalDateTime.now()
        return repository.save(entity)
    }

    open fun delete(id: Long) {
        val entity = findById(id)
        entity.deletedAt = LocalDateTime.now()
        repository.save(entity)
    }
}
