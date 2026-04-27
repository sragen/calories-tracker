package com.company.app.modules.subscription.repository

import com.company.app.modules.subscription.entity.PaymentEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentEventRepository : JpaRepository<PaymentEvent, Long>
