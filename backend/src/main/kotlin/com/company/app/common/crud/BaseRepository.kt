package com.company.app.common.crud

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseRepository<T : BaseEntity, ID> : JpaRepository<T, ID> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    fun findAllActive(pageable: Pageable): Page<T>

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deletedAt IS NULL")
    fun findActiveById(id: ID): T?
}
