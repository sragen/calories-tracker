package com.company.app.modules.food

import com.company.app.common.crud.BaseRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FoodItemRepository : BaseRepository<FoodItem, Long> {

    @Query("""
        SELECT f FROM FoodItem f
        WHERE f.deletedAt IS NULL AND f.isActive = TRUE
        AND (:q = '' OR LOWER(f.name) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(COALESCE(f.nameEn, '')) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:categoryId IS NULL OR f.category.id = :categoryId)
        ORDER BY f.isVerified DESC, f.name ASC
    """)
    fun search(
        @Param("q") q: String?,
        @Param("categoryId") categoryId: Long?,
        pageable: Pageable
    ): Page<FoodItem>

    fun findByBarcodeAndDeletedAtIsNull(barcode: String): FoodItem?

    @Query("""
        SELECT f FROM FoodItem f
        WHERE f.deletedAt IS NULL
        AND f.isVerified = FALSE
        ORDER BY f.createdAt DESC
    """)
    fun findPendingReview(pageable: Pageable): Page<FoodItem>
}

@Repository
interface FoodCategoryRepository : org.springframework.data.jpa.repository.JpaRepository<FoodCategory, Long> {
    fun findAllByOrderBySortOrderAsc(): List<FoodCategory>
}
