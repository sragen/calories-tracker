package com.company.app.modules.analytics

import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/analytics")
class AnalyticsController(private val analyticsService: AnalyticsService) {

    @GetMapping("/overview")
    fun overview() = analyticsService.getOverview()

    @GetMapping("/revenue")
    fun revenue(@RequestParam(defaultValue = "30") days: Int) =
        analyticsService.getRevenue(days.coerceIn(7, 90))

    @GetMapping("/top-foods")
    fun topFoods(@RequestParam(defaultValue = "20") limit: Int) =
        analyticsService.getTopFoods(limit.coerceIn(5, 50))

    @GetMapping("/export")
    fun export(@RequestParam(defaultValue = "overview") type: String): ResponseEntity<ByteArray> {
        val (filename, csv) = when (type) {
            "revenue"   -> "revenue.csv"   to analyticsService.exportRevenueCsv()
            "top-foods" -> "top-foods.csv" to analyticsService.exportTopFoodsCsv()
            else        -> "overview.csv"  to analyticsService.exportOverviewCsv()
        }
        return ResponseEntity.ok()
            .headers(HttpHeaders().apply {
                contentDisposition = ContentDisposition.attachment().filename(filename).build()
            })
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv.toByteArray())
    }
}
