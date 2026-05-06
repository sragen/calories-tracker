package com.company.app.modules.userfood

import com.company.app.common.crud.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_food_library")
class UserFoodLibrary(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "food_name", nullable = false)
    var foodName: String,

    @Column(name = "calories_per_100g", nullable = false, columnDefinition = "numeric(8,2)")
    var caloriesPer100g: Double,

    @Column(name = "protein_per_100g", columnDefinition = "numeric(6,2)")
    var proteinPer100g: Double = 0.0,

    @Column(name = "carbs_per_100g", columnDefinition = "numeric(6,2)")
    var carbsPer100g: Double = 0.0,

    @Column(name = "fat_per_100g", columnDefinition = "numeric(6,2)")
    var fatPer100g: Double = 0.0,

    @Column(name = "serving_size_g", columnDefinition = "numeric(6,2)")
    var servingSizeG: Double = 100.0,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "source")
    val source: String = "AI_SCAN",

    @Column(name = "original_food_id")
    val originalFoodId: Long? = null,

    @Column(name = "use_count")
    var useCount: Int = 0,

    @Column(name = "last_used_at")
    var lastUsedAt: LocalDateTime? = null,
) : BaseEntity()
