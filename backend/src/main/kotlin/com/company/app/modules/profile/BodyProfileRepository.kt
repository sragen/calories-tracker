package com.company.app.modules.profile

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BodyProfileRepository : JpaRepository<BodyProfile, Long> {
    fun findByUserId(userId: Long): BodyProfile?
}
