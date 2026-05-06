package com.company.app.modules.userfood

import com.company.app.common.crud.BaseRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserFoodLibraryRepository : BaseRepository<UserFoodLibrary, Long> {

    @Query("""
        SELECT u FROM UserFoodLibrary u
        WHERE u.userId = :userId AND u.deletedAt IS NULL
        AND (:search IS NULL OR LOWER(u.foodName) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY u.lastUsedAt DESC NULLS LAST, u.createdAt DESC
    """)
    fun findByUserIdOrderByLastUsed(
        @Param("userId") userId: Long,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<UserFoodLibrary>

    @Query("""
        SELECT u FROM UserFoodLibrary u
        WHERE u.userId = :userId AND u.deletedAt IS NULL
        AND (:search IS NULL OR LOWER(u.foodName) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY u.useCount DESC, u.lastUsedAt DESC NULLS LAST
    """)
    fun findByUserIdOrderByUseCount(
        @Param("userId") userId: Long,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<UserFoodLibrary>

    @Query("""
        SELECT u FROM UserFoodLibrary u
        WHERE u.userId = :userId AND u.deletedAt IS NULL
        AND (:search IS NULL OR LOWER(u.foodName) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY u.foodName ASC
    """)
    fun findByUserIdOrderByName(
        @Param("userId") userId: Long,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<UserFoodLibrary>

    fun findByUserIdAndFoodNameIgnoreCaseAndDeletedAtIsNull(userId: Long, foodName: String): UserFoodLibrary?

    fun findByUserIdAndIdAndDeletedAtIsNull(userId: Long, id: Long): UserFoodLibrary?
}
