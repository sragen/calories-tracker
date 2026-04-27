package com.company.app.modules.aiscan

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "ai_scan_logs")
class AiScanLog(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "raw_response", columnDefinition = "TEXT")
    var rawResponse: String? = null,

    @Column(name = "detected_count", nullable = false)
    var detectedCount: Int = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
