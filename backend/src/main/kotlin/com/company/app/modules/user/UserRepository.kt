package com.company.app.modules.user

import com.company.app.common.crud.BaseRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : BaseRepository<User, Long> {
    fun findByEmailAndDeletedAtIsNull(email: String): User?
    fun findByPhoneAndDeletedAtIsNull(phone: String): User?
    fun findByGoogleIdAndDeletedAtIsNull(googleId: String): User?

    @Query("""
        SELECT u FROM User u
        WHERE u.deletedAt IS NULL
          AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    fun searchActive(@Param("q") q: String, pageable: Pageable): Page<User>
}
