package com.company.app.modules.profile

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "body_profiles")
class BodyProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: Long,

    @Column(name = "height_cm", nullable = false, columnDefinition = "numeric(5,1)")
    var heightCm: Double,

    @Column(name = "weight_kg", nullable = false, columnDefinition = "numeric(5,1)")
    var weightKg: Double,

    @Column(name = "birth_date", nullable = false)
    var birthDate: LocalDate,

    @Column(nullable = false)
    var gender: String,

    @Column(name = "activity_level", nullable = false)
    var activityLevel: String = "SEDENTARY",

    @Column(nullable = false)
    var goal: String = "MAINTAIN",

    @Column(name = "target_weight_kg", columnDefinition = "numeric(5,1)")
    var targetWeightKg: Double? = null,

    @Column(name = "bmr_kcal", columnDefinition = "numeric(8,2)")
    var bmrKcal: Double? = null,

    @Column(name = "tdee_kcal", columnDefinition = "numeric(8,2)")
    var tdeeKcal: Double? = null,

    @Column(name = "recommended_calories", columnDefinition = "numeric(8,2)")
    var recommendedCalories: Double? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
