package com.company.app.modules.aiscan

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.util.Base64

@JsonIgnoreProperties(ignoreUnknown = true)
private data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)
@JsonIgnoreProperties(ignoreUnknown = true)
private data class GeminiCandidate(val content: GeminiContent? = null)
@JsonIgnoreProperties(ignoreUnknown = true)
private data class GeminiContent(val parts: List<GeminiPart>? = null)
@JsonIgnoreProperties(ignoreUnknown = true)
private data class GeminiPart(val text: String? = null)

data class DetectedFoodRaw(
    val name: String,
    val portionG: Double,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double
)

@Service
class GeminiService(
    @Value("\${app.gemini.api-key}") private val apiKey: String,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val restClient = RestClient.builder()
        .baseUrl("https://generativelanguage.googleapis.com/v1beta/models")
        .build()

    private val prompt = """
        Analisis gambar makanan ini dan identifikasi semua makanan yang terlihat.
        Untuk setiap makanan, berikan estimasi berdasarkan porsi yang terlihat.

        Kembalikan HANYA JSON array, tidak ada teks lain:
        [{"name":"Nasi Putih","portionG":200,"caloriesPer100g":175,"proteinPer100g":3.1,"carbsPer100g":38.9,"fatPer100g":0.3}]

        Gunakan nama makanan dalam bahasa Indonesia.
        Jika tidak ada makanan terdeteksi, kembalikan array kosong: []
    """.trimIndent()

    fun analyzeImage(imageBytes: ByteArray, mimeType: String = "image/jpeg"): List<DetectedFoodRaw> {
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)
        val requestBody = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to prompt),
                        mapOf("inline_data" to mapOf(
                            "mime_type" to mimeType,
                            "data" to base64Image
                        ))
                    )
                )
            ),
            "generationConfig" to mapOf(
                "temperature" to 0.1,
                "maxOutputTokens" to 1024
            )
        )

        return try {
            val response = restClient.post()
                .uri("/gemini-1.5-flash:generateContent?key=$apiKey")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(GeminiResponse::class.java)

            val text = response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "[]"
            val cleaned = text.trim().removePrefix("```json").removeSuffix("```").trim()
            objectMapper.readValue(cleaned, objectMapper.typeFactory.constructCollectionType(List::class.java, DetectedFoodRaw::class.java))
        } catch (e: Exception) {
            log.error("Gemini analysis failed: ${e.message}")
            emptyList()
        }
    }
}
