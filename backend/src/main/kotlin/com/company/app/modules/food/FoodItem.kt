package com.company.app.modules.food

import com.company.app.common.crud.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "food_items")
class FoodItem(
    @Column(nullable = false)
    var name: String,

    @Column(name = "name_en")
    var nameEn: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: FoodCategory? = null,

    @Column(name = "calories_per_100g", nullable = false, columnDefinition = "numeric(8,2)")
    var caloriesPer100g: Double,

    @Column(name = "protein_per_100g", nullable = false, columnDefinition = "numeric(8,2)")
    var proteinPer100g: Double = 0.0,

    @Column(name = "carbs_per_100g", nullable = false, columnDefinition = "numeric(8,2)")
    var carbsPer100g: Double = 0.0,

    @Column(name = "fat_per_100g", nullable = false, columnDefinition = "numeric(8,2)")
    var fatPer100g: Double = 0.0,

    @Column(name = "fiber_per_100g", columnDefinition = "numeric(8,2)")
    var fiberPer100g: Double? = null,

    @Column(name = "sugar_per_100g", columnDefinition = "numeric(8,2)")
    var sugarPer100g: Double? = null,

    @Column(name = "sodium_per_100mg", columnDefinition = "numeric(8,2)")
    var sodiumPer100mg: Double? = null,

    @Column(name = "default_serving_g", nullable = false, columnDefinition = "numeric(8,2)")
    var defaultServingG: Double = 100.0,

    @Column(name = "serving_description")
    var servingDescription: String? = null,

    @Column(unique = true)
    var barcode: String? = null,

    @Column(nullable = false)
    var source: String = "ADMIN",

    @Column(name = "external_id")
    var externalId: String? = null,

    @Column(name = "is_verified", nullable = false)
    var isVerified: Boolean = false,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_by")
    var createdBy: Long? = null
) : BaseEntity()
