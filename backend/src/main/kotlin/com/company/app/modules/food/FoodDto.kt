package com.company.app.modules.food

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class FoodItemResponse(
    val id: Long,
    val name: String,
    val nameEn: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val fiberPer100g: Double?,
    val defaultServingG: Double,
    val servingDescription: String?,
    val barcode: String?,
    val source: String,
    val isVerified: Boolean,
    val createdAt: LocalDateTime
)

data class FoodCategoryResponse(
    val id: Long,
    val name: String,
    val nameEn: String?,
    val icon: String?
)

data class FoodItemRequest(
    @field:NotBlank val name: String,
    val nameEn: String? = null,
    val categoryId: Long? = null,
    @field:NotNull @field:DecimalMin("0.0") val caloriesPer100g: Double,
    @field:DecimalMin("0.0") val proteinPer100g: Double = 0.0,
    @field:DecimalMin("0.0") val carbsPer100g: Double = 0.0,
    @field:DecimalMin("0.0") val fatPer100g: Double = 0.0,
    val fiberPer100g: Double? = null,
    @field:DecimalMin("1.0") val defaultServingG: Double = 100.0,
    val servingDescription: String? = null,
    val barcode: String? = null
)

fun FoodItem.toResponse() = FoodItemResponse(
    id = id,
    name = name,
    nameEn = nameEn,
    categoryId = category?.id,
    categoryName = category?.name,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    carbsPer100g = carbsPer100g,
    fatPer100g = fatPer100g,
    fiberPer100g = fiberPer100g,
    defaultServingG = defaultServingG,
    servingDescription = servingDescription,
    barcode = barcode,
    source = source,
    isVerified = isVerified,
    createdAt = createdAt
)

fun FoodCategory.toResponse() = FoodCategoryResponse(
    id = id,
    name = name,
    nameEn = nameEn,
    icon = icon
)
