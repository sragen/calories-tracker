package com.company.app.modules.config

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AppConfigRepository : JpaRepository<AppConfig, Long> {
    fun findByKey(key: String): AppConfig?
    fun findByIsActiveTrue(): List<AppConfig>
}
