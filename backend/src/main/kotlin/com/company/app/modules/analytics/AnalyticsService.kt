package com.company.app.modules.analytics

import com.company.app.modules.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

data class OverviewResponse(
    val totalUsers: Long,
    val dau: Long,
    val mau: Long,
    val totalLogs: Long,
    val activePremium: Long,
    val totalRevenueIdr: Long
)

data class RevenuePoint(val date: String, val revenueIdr: Long)

data class TopFoodEntry(val id: Long, val name: String, val logCount: Long)

@Service
class AnalyticsService(
    private val analyticsRepo: AnalyticsRepository,
    private val userRepo: UserRepository
) {
    fun getOverview(): OverviewResponse {
        val today = LocalDate.now()
        return OverviewResponse(
            totalUsers     = userRepo.count(),
            dau            = analyticsRepo.countDau(today),
            mau            = analyticsRepo.countMau(today.minusDays(30)),
            totalLogs      = analyticsRepo.countTotalLogs(),
            activePremium  = analyticsRepo.countActivePremium(),
            totalRevenueIdr = analyticsRepo.totalRevenue()
        )
    }

    fun getRevenue(days: Int = 30): List<RevenuePoint> {
        val since = LocalDate.now().minusDays(days.toLong())
        return analyticsRepo.revenueByDay(since).map { (day, rev) ->
            RevenuePoint(date = day, revenueIdr = rev)
        }
    }

    fun getTopFoods(limit: Int = 20): List<TopFoodEntry> =
        analyticsRepo.topFoods(limit).map { (id, name, count) ->
            TopFoodEntry(id = id, name = name, logCount = count)
        }

    fun exportOverviewCsv(): String {
        val ov = getOverview()
        return buildString {
            appendLine("metric,value")
            appendLine("total_users,${ov.totalUsers}")
            appendLine("dau,${ov.dau}")
            appendLine("mau,${ov.mau}")
            appendLine("total_logs,${ov.totalLogs}")
            appendLine("active_premium,${ov.activePremium}")
            appendLine("total_revenue_idr,${ov.totalRevenueIdr}")
        }
    }

    fun exportRevenueCsv(): String {
        val rows = getRevenue(90)
        return buildString {
            appendLine("date,revenue_idr")
            rows.forEach { appendLine("${it.date},${it.revenueIdr}") }
        }
    }

    fun exportTopFoodsCsv(): String {
        val rows = getTopFoods(50)
        return buildString {
            appendLine("rank,food_id,name,log_count")
            rows.forEachIndexed { i, f -> appendLine("${i + 1},${f.id},\"${f.name.replace("\"", "\"\"")}\",${f.logCount}") }
        }
    }
}
