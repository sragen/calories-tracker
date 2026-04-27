package com.company.app.modules.analytics

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AnalyticsRepository {

    @PersistenceContext
    private lateinit var em: EntityManager

    private fun scalar(sql: String, vararg params: Pair<String, Any>): Long {
        val q = em.createNativeQuery(sql)
        params.forEach { (k, v) -> q.setParameter(k, v) }
        return (q.singleResult as Number).toLong()
    }

    fun countDau(today: LocalDate): Long = scalar(
        "SELECT COUNT(DISTINCT user_id) FROM meal_logs WHERE logged_at = :d AND deleted_at IS NULL",
        "d" to today
    )

    fun countMau(since: LocalDate): Long = scalar(
        "SELECT COUNT(DISTINCT user_id) FROM meal_logs WHERE logged_at >= :s AND deleted_at IS NULL",
        "s" to since
    )

    fun countTotalLogs(): Long = scalar(
        "SELECT COUNT(*) FROM meal_logs WHERE deleted_at IS NULL"
    )

    fun countActivePremium(): Long = scalar(
        "SELECT COUNT(*) FROM user_subscriptions WHERE status = 'ACTIVE' AND expires_at > NOW()"
    )

    fun totalRevenue(): Long = scalar("""
        SELECT COALESCE(SUM(sp.price_idr), 0)
        FROM user_subscriptions us
        JOIN subscription_plans sp ON us.plan_id = sp.id
        WHERE us.status IN ('ACTIVE', 'EXPIRED')
    """)

    @Suppress("UNCHECKED_CAST")
    fun revenueByDay(since: LocalDate): List<Pair<String, Long>> {
        val rows = em.createNativeQuery("""
            SELECT TO_CHAR(us.started_at::date, 'YYYY-MM-DD'), COALESCE(SUM(sp.price_idr), 0)
            FROM user_subscriptions us
            JOIN subscription_plans sp ON us.plan_id = sp.id
            WHERE us.status IN ('ACTIVE', 'EXPIRED') AND us.started_at >= :s
            GROUP BY us.started_at::date
            ORDER BY us.started_at::date
        """).setParameter("s", since).resultList as List<Array<Any>>
        return rows.map { Pair(it[0] as String, (it[1] as Number).toLong()) }
    }

    @Suppress("UNCHECKED_CAST")
    fun topFoods(limit: Int): List<Triple<Long, String, Long>> {
        val rows = em.createNativeQuery("""
            SELECT f.id, f.name, COUNT(m.id) as log_count
            FROM meal_logs m
            JOIN food_items f ON m.food_item_id = f.id
            WHERE m.deleted_at IS NULL
            GROUP BY f.id, f.name
            ORDER BY log_count DESC
            LIMIT :lim
        """).setParameter("lim", limit).resultList as List<Array<Any>>
        return rows.map { Triple((it[0] as Number).toLong(), it[1] as String, (it[2] as Number).toLong()) }
    }
}
