package com.company.app.modules.user

import com.company.app.common.crud.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : BaseRepository<User, Long> {
    fun findByEmailAndDeletedAtIsNull(email: String): User?
    fun findByPhoneAndDeletedAtIsNull(phone: String): User?
}
