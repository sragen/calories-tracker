package com.company.app.modules.meal

import com.company.app.common.crud.BaseEntity
import com.company.app.modules.food.FoodItem
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "meal_logs")
class MealLog(
    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "food_item_id", nullable = false)
    var foodItem: FoodItem,

    @Column(name = "quantity_g", nullable = false, columnDefinition = "numeric(8,2)")
    var quantityG: Double,

    @Column(name = "meal_type", nullable = false)
    var mealType: String = "SNACK",

    @Column(name = "calories_snapshot", nullable = false, columnDefinition = "numeric(8,2)")
    var caloriesSnapshot: Double,

    @Column(name = "protein_g_snapshot", nullable = false, columnDefinition = "numeric(8,2)")
    var proteinGSnapshot: Double = 0.0,

    @Column(name = "carbs_g_snapshot", nullable = false, columnDefinition = "numeric(8,2)")
    var carbsGSnapshot: Double = 0.0,

    @Column(name = "fat_g_snapshot", nullable = false, columnDefinition = "numeric(8,2)")
    var fatGSnapshot: Double = 0.0,

    @Column(name = "ai_scan_photo_url")
    var aiScanPhotoUrl: String? = null,

    @Column(name = "logged_at", nullable = false)
    var loggedAt: LocalDate = LocalDate.now()
) : BaseEntity()
