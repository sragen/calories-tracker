package com.company.app.modules.food

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class OpenFoodFactsClient {

    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.builder()
        .baseUrl("https://world.openfoodfacts.org/api/v0")
        .defaultHeader("User-Agent", "CaloriesTrackerApp/1.0 (contact@yourdomain.com)")
        .build()

    fun findByBarcode(barcode: String): OFFResult? {
        return try {
            val resp = restClient.get()
                .uri("/product/{barcode}.json", barcode)
                .retrieve()
                .body(OFFResponse::class.java)

            if (resp?.status != 1 || resp.product == null) return null

            val p = resp.product
            val n = p.nutriments ?: return null
            val calories = n.caloriesPer100g ?: return null
            val name = p.productNameId?.takeIf { it.isNotBlank() }
                ?: p.productName?.takeIf { it.isNotBlank() }
                ?: return null

            OFFResult(
                name = name,
                nameEn = if (p.productNameId != null && p.productName != null && p.productName != p.productNameId) p.productName else null,
                caloriesPer100g = calories,
                proteinPer100g = n.proteinPer100g ?: 0.0,
                carbsPer100g = n.carbsPer100g ?: 0.0,
                fatPer100g = n.fatPer100g ?: 0.0,
                fiberPer100g = n.fiberPer100g,
                barcode = barcode
            )
        } catch (e: Exception) {
            log.warn("OpenFoodFacts lookup failed for barcode={}: {}", barcode, e.message)
            null
        }
    }

    data class OFFResult(
        val name: String,
        val nameEn: String?,
        val caloriesPer100g: Double,
        val proteinPer100g: Double,
        val carbsPer100g: Double,
        val fatPer100g: Double,
        val fiberPer100g: Double?,
        val barcode: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class OFFResponse(
        val status: Int = 0,
        val product: OFFProduct? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class OFFProduct(
        @JsonProperty("product_name") val productName: String? = null,
        @JsonProperty("product_name_id") val productNameId: String? = null,
        val nutriments: OFFNutriments? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class OFFNutriments(
        @JsonProperty("energy-kcal_100g") val caloriesPer100g: Double? = null,
        @JsonProperty("proteins_100g") val proteinPer100g: Double? = null,
        @JsonProperty("carbohydrates_100g") val carbsPer100g: Double? = null,
        @JsonProperty("fat_100g") val fatPer100g: Double? = null,
        @JsonProperty("fiber_100g") val fiberPer100g: Double? = null
    )
}
