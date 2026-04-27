package com.company.app.modules.food

import com.company.app.common.auth.UserPrincipal
import com.company.app.common.rbac.RequiresPermission
import com.company.app.common.rbac.RequiresPermission.Action
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/foods")
class FoodController(private val foodService: FoodService) {

    @GetMapping
    fun search(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) categoryId: Long?,
        @PageableDefault(size = 20) pageable: Pageable
    ) = foodService.search(q, categoryId, pageable)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long) = foodService.findById(id)

    @GetMapping("/barcode/{barcode}")
    fun findByBarcode(@PathVariable barcode: String) = foodService.findByBarcode(barcode)

    @GetMapping("/categories")
    fun getCategories() = foodService.getCategories()

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.CREATED)
    fun submit(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: FoodItemRequest
    ) = foodService.create(req, principal.id, isAdmin = false)
}

@RestController
@RequestMapping("/api/admin/foods")
class AdminFoodController(private val foodService: FoodService) {

    @GetMapping
    @RequiresPermission(module = "FOODS", action = Action.READ)
    fun list(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) source: String?,
        @RequestParam(required = false) pendingOnly: Boolean = false,
        @PageableDefault(size = 20) pageable: Pageable
    ) = when {
        pendingOnly -> foodService.getPendingReview(pageable)
        else -> foodService.search(q, categoryId, pageable)
    }

    @GetMapping("/pending")
    @RequiresPermission(module = "FOODS", action = Action.READ)
    fun pending(@PageableDefault(size = 20) pageable: Pageable) =
        foodService.getPendingReview(pageable)

    @GetMapping("/{id}")
    @RequiresPermission(module = "FOODS", action = Action.READ)
    fun findById(@PathVariable id: Long) = foodService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequiresPermission(module = "FOODS", action = Action.WRITE)
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: FoodItemRequest
    ) = foodService.create(req, principal.id, isAdmin = true)

    @PutMapping("/{id}")
    @RequiresPermission(module = "FOODS", action = Action.WRITE)
    fun update(@PathVariable id: Long, @Valid @RequestBody req: FoodItemRequest) =
        foodService.update(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequiresPermission(module = "FOODS", action = Action.DELETE)
    fun delete(@PathVariable id: Long) = foodService.delete(id)

    @PostMapping("/{id}/verify")
    @RequiresPermission(module = "FOODS", action = Action.WRITE)
    fun verify(@PathVariable id: Long) = foodService.verify(id)

    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequiresPermission(module = "FOODS", action = Action.WRITE)
    fun reject(@PathVariable id: Long) = foodService.reject(id)

    @PostMapping("/import-csv")
    @RequiresPermission(module = "FOODS", action = Action.WRITE)
    fun importCsv(@RequestParam("file") file: MultipartFile): FoodService.ImportResult =
        foodService.importCsv(file)
}
