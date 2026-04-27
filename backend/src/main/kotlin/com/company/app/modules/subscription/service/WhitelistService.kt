package com.company.app.modules.subscription.service

import com.company.app.common.exception.AppException
import com.company.app.modules.subscription.dto.WhitelistEntryResponse
import com.company.app.modules.subscription.entity.PremiumWhitelist
import com.company.app.modules.subscription.repository.PremiumWhitelistRepository
import com.company.app.modules.user.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WhitelistService(
    private val whitelistRepository: PremiumWhitelistRepository,
    private val userRepository: UserRepository
) {
    fun findAll(pageable: Pageable): Page<WhitelistEntryResponse> =
        whitelistRepository.findAllPaged(pageable).map { it.toResponse() }

    @Transactional
    fun add(userId: Long, note: String?, addedBy: Long): WhitelistEntryResponse {
        userRepository.findActiveById(userId)
            ?: throw AppException.notFound("User not found")

        if (whitelistRepository.existsByUserId(userId))
            throw AppException.conflict("User is already on the whitelist")

        val entry = whitelistRepository.save(PremiumWhitelist(userId = userId, addedBy = addedBy, note = note))
        return entry.toResponse()
    }

    @Transactional
    fun remove(userId: Long) {
        if (!whitelistRepository.existsByUserId(userId))
            throw AppException.notFound("User is not on the whitelist")
        whitelistRepository.deleteByUserId(userId)
    }

    private fun PremiumWhitelist.toResponse(): WhitelistEntryResponse {
        val user = userRepository.findActiveById(userId)
        val addedByUser = userRepository.findActiveById(addedBy)
        return WhitelistEntryResponse(
            id = id,
            userId = userId,
            userName = user?.name ?: "Unknown",
            userEmail = user?.email,
            note = note,
            addedByName = addedByUser?.name ?: "Unknown",
            createdAt = createdAt
        )
    }
}
