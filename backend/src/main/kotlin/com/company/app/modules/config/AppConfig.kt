package com.company.app.modules.config

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "app_configs")
class AppConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val key: String,

    @Column(nullable = false)
    var value: String,

    @Column(nullable = false)
    val type: String = "BOOLEAN",

    @Column
    val label: String? = null,

    @Column
    val description: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_by")
    var updatedBy: Long? = null
)
