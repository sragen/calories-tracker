package com.company.app.modules.user

import com.company.app.common.auth.UserResponse
import com.company.app.common.crud.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Column(unique = true)
    var email: String? = null,

    @Column(unique = true)
    var phone: String? = null,

    @Column
    var password: String? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var role: String = "USER",

    @Column(nullable = false)
    var status: String = "ACTIVE",

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "fcm_token")
    var fcmToken: String? = null,

    @Column(name = "last_login")
    var lastLogin: LocalDateTime? = null
) : BaseEntity() {

    fun toResponse() = UserResponse(
        id = id,
        email = email,
        phone = phone,
        name = name,
        role = role,
        status = status,
        avatarUrl = avatarUrl,
        createdAt = createdAt
    )
}
