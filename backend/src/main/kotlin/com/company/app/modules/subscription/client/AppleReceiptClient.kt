package com.company.app.modules.subscription.client

import com.company.app.common.exception.AppException
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import org.springframework.stereotype.Component
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Base64

@Component
class AppleReceiptClient(private val objectMapper: ObjectMapper) {

    data class ValidationResult(
        val originalTransactionId: String,
        val transactionId: String,
        val status: String,
        val currentPeriodStart: LocalDateTime,
        val currentPeriodEnd: LocalDateTime,
        val trialEndsAt: LocalDateTime?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TransactionPayload(
        val originalTransactionId: String? = null,
        val transactionId: String? = null,
        val purchaseDate: Long? = null,
        val originalPurchaseDate: Long? = null,
        val expiresDate: Long? = null,
        val type: String? = null,
        val environment: String? = null,
        val offerType: Int? = null
    )

    fun validate(jwsTransactionInfo: String): ValidationResult {
        val payload = decodeAndVerifyJws(jwsTransactionInfo)
        val tx = objectMapper.readValue(payload, TransactionPayload::class.java)

        val originalTxId = tx.originalTransactionId
            ?: throw AppException.badRequest("Invalid Apple receipt: missing originalTransactionId")
        val txId = tx.transactionId
            ?: throw AppException.badRequest("Invalid Apple receipt: missing transactionId")
        val expiresMs = tx.expiresDate
            ?: throw AppException.badRequest("Invalid Apple receipt: missing expiresDate")
        val purchaseMs = tx.purchaseDate
            ?: throw AppException.badRequest("Invalid Apple receipt: missing purchaseDate")

        val periodEnd = Instant.ofEpochMilli(expiresMs).toLocalDateTime()
        val periodStart = Instant.ofEpochMilli(purchaseMs).toLocalDateTime()

        val isInTrial = tx.offerType == 2
        val status = if (isInTrial) "TRIAL" else "ACTIVE"

        return ValidationResult(
            originalTransactionId = originalTxId,
            transactionId = txId,
            status = status,
            currentPeriodStart = periodStart,
            currentPeriodEnd = periodEnd,
            trialEndsAt = if (isInTrial) periodEnd else null
        )
    }

    private fun decodeAndVerifyJws(compactJws: String): String {
        val jwsObject = JWSObject.parse(compactJws)
        val x5c = jwsObject.header.x509CertChain
            ?: throw AppException.badRequest("Invalid Apple JWS: missing x5c header")

        val factory = CertificateFactory.getInstance("X.509")
        val certs = x5c.map { base64 ->
            factory.generateCertificate(
                Base64.getDecoder().decode(base64.decode()).inputStream()
            ) as X509Certificate
        }

        // Verify chain: each cert signed by the next
        for (i in 0 until certs.size - 1) {
            try {
                certs[i].verify(certs[i + 1].publicKey)
            } catch (e: Exception) {
                throw AppException.badRequest("Invalid Apple JWS: certificate chain verification failed")
            }
        }

        // Verify JWS signature with leaf cert public key
        val leafPublicKey = certs[0].publicKey as? ECPublicKey
            ?: throw AppException.badRequest("Invalid Apple JWS: leaf cert is not EC key")

        val ecKey = ECKey.Builder(Curve.P_256, leafPublicKey).build()
        val verifier = ECDSAVerifier(ecKey)

        if (!jwsObject.verify(verifier)) {
            throw AppException.badRequest("Invalid Apple JWS: signature verification failed")
        }

        return jwsObject.payload.toString()
    }

    private fun Instant.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(this, ZoneId.of("UTC"))
}
