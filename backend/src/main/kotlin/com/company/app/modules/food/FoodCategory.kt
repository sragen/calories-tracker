package com.company.app.modules.food

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "food_categories")
class FoodCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(name = "name_en")
    val nameEn: String? = null,

    @Column
    val icon: String? = null,

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
