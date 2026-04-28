package com.company.app.modules.goal

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "daily_goals")
class DailyGoal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: Long,

    @Column(name = "target_calories", nullable = false, columnDefinition = "numeric(8,2)")
    var targetCalories: Double,

    @Column(name = "target_protein_g", nullable = false, columnDefinition = "numeric(8,2)")
    var targetProteinG: Double = 0.0,

    @Column(name = "target_carbs_g", nullable = false, columnDefinition = "numeric(8,2)")
    var targetCarbsG: Double = 0.0,

    @Column(name = "target_fat_g", nullable = false, columnDefinition = "numeric(8,2)")
    var targetFatG: Double = 0.0,

    @Column(name = "auto_calculated", nullable = false)
    var autoCalculated: Boolean = true,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
