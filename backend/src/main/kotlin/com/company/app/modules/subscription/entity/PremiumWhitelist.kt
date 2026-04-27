package com.company.app.modules.subscription.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "premium_whitelists")
class PremiumWhitelist(
    @Column(name = "user_id", nullable = false, unique = true)
    val userId: Long,

    @Column(name = "added_by", nullable = false)
    val addedBy: Long,

    @Column
    var note: String? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime
}
