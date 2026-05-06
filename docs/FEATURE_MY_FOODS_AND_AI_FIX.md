# Feature Spec: My Foods Library & AI Scan Accuracy Fix

> **Status**: Design Complete — Ready for Implementation  
> **Next Migration**: V19, V20  
> **Affects**: `backend/modules/aiscan`, `backend/modules/goal`, new `backend/modules/userfood`

---

## 1. Current State (What Already Exists)

Before implementing, note what is **already working** — do not re-implement:

| Feature | Status | Location |
|---|---|---|
| Upload scan image to MinIO | ✅ Done | `AiScanService.kt:53` |
| `imageUrl` field on `AiScanLog` | ✅ Done | `AiScanLog.kt:13` |
| `imageUrl` returned in `AiScanResponse` | ✅ Done | `AiScanDto.kt:16` |
| DailyGoal with calorie + macro targets | ✅ Done | `DailyGoal.kt` |
| BMR/TDEE calculation with LOSE/MAINTAIN/GAIN | ✅ Done | `BmrCalculator.kt` |
| `syncFromBmr()` to auto-populate goals | ✅ Done | `DailyGoalService.kt:35` |
| `PUT /api/goals/daily` manual override | ✅ Done | `DailyGoalController.kt:19` |

**What is missing:**
- `user_food_library` table and module (My Foods feature)
- `GET /api/goals/daily/presets` endpoint (Preset + Override UI)
- Gemini prompt update for gramasi accuracy

---

## 2. Feature: My Foods Library

### Overview
Users can save any food from AI Scan or Barcode to a personal library. From the library, they can log the same food again without re-scanning.

### Database Schema

**Migration: `V19__user_food_library.sql`**

```sql
CREATE TABLE user_food_library (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    food_name       VARCHAR(255) NOT NULL,
    calories_per_100g  NUMERIC(8,2) NOT NULL,
    protein_per_100g   NUMERIC(6,2) NOT NULL DEFAULT 0,
    carbs_per_100g     NUMERIC(6,2) NOT NULL DEFAULT 0,
    fat_per_100g       NUMERIC(6,2) NOT NULL DEFAULT 0,
    serving_size_g     NUMERIC(6,2) NOT NULL DEFAULT 100,
    image_url          VARCHAR(512),
    source             VARCHAR(50) NOT NULL DEFAULT 'AI_SCAN',  -- AI_SCAN | BARCODE | MANUAL
    original_food_id   BIGINT REFERENCES food_items(id),        -- nullable, linked global food
    use_count          INTEGER NOT NULL DEFAULT 0,
    last_used_at       TIMESTAMP,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_food_library_user_id ON user_food_library(user_id);
CREATE INDEX idx_user_food_library_last_used ON user_food_library(user_id, last_used_at DESC);
CREATE INDEX idx_user_food_library_use_count ON user_food_library(user_id, use_count DESC);
```

### Module Structure

```
backend/modules/userfood/
├── UserFoodLibrary.kt          -- Entity
├── UserFoodLibraryRepository.kt
├── UserFoodLibraryDto.kt
├── UserFoodLibraryService.kt
└── UserFoodLibraryController.kt
```

### Entity

```kotlin
@Entity
@Table(name = "user_food_library")
class UserFoodLibrary(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "food_name", nullable = false)
    var foodName: String,

    @Column(name = "calories_per_100g", nullable = false)
    var caloriesPer100g: Double,

    @Column(name = "protein_per_100g")
    var proteinPer100g: Double = 0.0,

    @Column(name = "carbs_per_100g")
    var carbsPer100g: Double = 0.0,

    @Column(name = "fat_per_100g")
    var fatPer100g: Double = 0.0,

    @Column(name = "serving_size_g")
    var servingSizeG: Double = 100.0,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "source")
    val source: String = "AI_SCAN",

    @Column(name = "original_food_id")
    val originalFoodId: Long? = null,

    @Column(name = "use_count")
    var useCount: Int = 0,

    @Column(name = "last_used_at")
    var lastUsedAt: LocalDateTime? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
```

### DTOs

```kotlin
// Request: save a food to library (from AI scan result or manually)
data class SaveUserFoodRequest(
    @field:NotBlank val foodName: String,
    @field:DecimalMin("0.0") val caloriesPer100g: Double,
    @field:DecimalMin("0.0") val proteinPer100g: Double = 0.0,
    @field:DecimalMin("0.0") val carbsPer100g: Double = 0.0,
    @field:DecimalMin("0.0") val fatPer100g: Double = 0.0,
    @field:DecimalMin("1.0") val servingSizeG: Double = 100.0,
    val imageUrl: String? = null,
    val source: String = "AI_SCAN",
    val originalFoodId: Long? = null
)

// Response: food library item
data class UserFoodResponse(
    val id: Long,
    val foodName: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val servingSizeG: Double,
    val imageUrl: String?,
    val source: String,
    val useCount: Int,
    val lastUsedAt: LocalDateTime?,
    val totalCalories: Double  // computed: caloriesPer100g * servingSizeG / 100
)

// Sort options
enum class UserFoodSort { RECENTLY_USED, MOST_USED, ALPHABETICAL }
```

### API Endpoints

```
GET    /api/user-foods               List saved foods (sort, search)
POST   /api/user-foods               Save food to library
POST   /api/user-foods/{id}/log      Quick-log food to meal
DELETE /api/user-foods/{id}          Remove from library
```

#### `GET /api/user-foods`

Query params:
- `sort`: `RECENTLY_USED` (default) | `MOST_USED` | `ALPHABETICAL`
- `search`: optional string, filter by `food_name ILIKE %search%`
- `page`, `size`: pagination (default size=20)

Response:
```json
{
  "content": [
    {
      "id": 1,
      "foodName": "Ayam Bakar",
      "caloriesPer100g": 165.0,
      "proteinPer100g": 31.0,
      "carbsPer100g": 0.0,
      "fatPer100g": 3.6,
      "servingSizeG": 150.0,
      "imageUrl": "https://api.adikur.com/media/calsnap/ai-scans/42/uuid.jpg",
      "source": "AI_SCAN",
      "useCount": 5,
      "lastUsedAt": "2026-05-03T12:30:00",
      "totalCalories": 247.5
    }
  ],
  "totalElements": 12,
  "page": 0,
  "size": 20
}
```

#### `POST /api/user-foods`

Request body: `SaveUserFoodRequest`  
Response: `UserFoodResponse`  
Note: Duplicate check by `(userId, foodName)` — return existing if found (idempotent).

#### `POST /api/user-foods/{id}/log`

Quick-log a saved food to a meal without re-scanning.

Request body:
```json
{
  "mealType": "LUNCH",
  "portionG": 150.0,
  "loggedAt": "2026-05-06"
}
```

Response:
```json
{ "mealLogId": 99, "logged": true }
```

Side effects: increments `use_count`, sets `last_used_at = now()`.

#### `DELETE /api/user-foods/{id}`

Deletes the library entry. Does **not** delete the MinIO image (image is shared with scan log).  
Returns `204 No Content`.

### Service Logic Notes

- `log()` call: create `MealLog` using snapshots calculated from `caloriesPer100g * portionG / 100`. Reuse the same pattern in `AiScanService.confirm()`.
- Duplicate detection on `save()`: query `findByUserIdAndFoodNameIgnoreCase()`, return existing if match found.
- Authorization: all endpoints require authenticated user; service layer validates `userFoodLibrary.userId == principal.id`.

---

## 3. Feature: Goal Presets (Preset + Override UI)

### What Already Exists
- `BmrCalculator.calculate()` already computes recommended values for LOSE/MAINTAIN/GAIN
- `DailyGoalService.syncFromBmr()` already populates goals from BMR result
- `PUT /api/goals/daily` already handles manual override

### What's Missing
A single endpoint so the mobile UI can **display** preset options before user selects one.

### New Endpoint: `GET /api/goals/daily/presets`

Returns three preset options computed from the user's current body profile.

Response:
```json
{
  "presets": [
    {
      "type": "LOSE",
      "label": "Cut (Deficit)",
      "targetCalories": 1650.0,
      "targetProteinG": 124.0,
      "targetCarbsG": 165.0,
      "targetFatG": 55.0,
      "calorieAdjustment": -500
    },
    {
      "type": "MAINTAIN",
      "label": "Maintain",
      "targetCalories": 2150.0,
      "targetProteinG": 134.0,
      "targetCarbsG": 269.0,
      "targetFatG": 60.0,
      "calorieAdjustment": 0
    },
    {
      "type": "GAIN",
      "label": "Bulk (Surplus)",
      "targetCalories": 2450.0,
      "targetProteinG": 153.0,
      "targetCarbsG": 336.0,
      "targetFatG": 54.0,
      "calorieAdjustment": 300
    }
  ],
  "current": {
    "targetCalories": 2000.0,
    "targetProteinG": 125.0,
    "targetCarbsG": 250.0,
    "targetFatG": 55.0,
    "autoCalculated": false
  }
}
```

Returns `400 Bad Request` if user has no body profile yet.

### Implementation in `DailyGoalController`

```kotlin
@GetMapping("/presets")
fun presets(@AuthenticationPrincipal principal: UserPrincipal) =
    dailyGoalService.getPresets(principal.id)
```

### `DailyGoalService.getPresets()` logic

```kotlin
fun getPresets(userId: Long): GoalPresetsResponse {
    val profile = bodyProfileRepository.findByUserId(userId)
        ?: throw AppException.badRequest("Body profile belum diisi")
    
    val presets = listOf("LOSE", "MAINTAIN", "GAIN").map { goal ->
        val bmr = BmrCalculator.calculate(
            weightKg = profile.weightKg,
            heightCm = profile.heightCm,
            birthDate = profile.birthDate,
            gender = profile.gender,
            activityLevel = profile.activityLevel,
            goal = goal
        )
        GoalPreset(
            type = goal,
            label = when (goal) { "LOSE" -> "Cut (Deficit)"; "GAIN" -> "Bulk (Surplus)"; else -> "Maintain" },
            targetCalories = bmr.recommendedCalories,
            targetProteinG = bmr.recommendedProteinG,
            targetCarbsG = bmr.recommendedCarbsG,
            targetFatG = bmr.recommendedFatG,
            calorieAdjustment = when (goal) { "LOSE" -> -500; "GAIN" -> 300; else -> 0 }
        )
    }
    
    val current = getByUserId(userId)
    return GoalPresetsResponse(presets = presets, current = current)
}
```

When user selects a preset and optionally overrides values, mobile calls the **existing** `PUT /api/goals/daily` — no new endpoint needed for saving.

---

## 4. AI Scan: Gramasi Accuracy Fix

### Problem
Gemini consistently overestimates food weight by ~20–30%. Root cause: photos make food appear larger than its actual weight due to 2D perspective and depth loss.

### Fix Location
`GeminiService.kt` — update the `prompt` val.

### Current Prompt (line 41–50)

```kotlin
private val prompt = """
    Analisis gambar makanan ini dan identifikasi semua makanan yang terlihat.
    Untuk setiap makanan, berikan estimasi berdasarkan porsi yang terlihat.

    Kembalikan HANYA JSON array, tidak ada teks lain:
    [{"name":"Nasi Putih","portionG":200,...}]

    Gunakan nama makanan dalam bahasa Indonesia.
    Jika tidak ada makanan terdeteksi, kembalikan array kosong: []
""".trimIndent()
```

### Proposed Prompt Replacement

```kotlin
private val prompt = """
    Analisis gambar makanan ini dan identifikasi semua makanan yang terlihat.
    Untuk setiap makanan, berikan estimasi nutrisi berdasarkan porsi yang terlihat.

    ATURAN ESTIMASI BERAT (WAJIB DIIKUTI):
    - Foto 2D membuat makanan tampak 20-30% lebih besar dari berat sebenarnya.
      Kompensasi dengan selalu memilih estimasi yang LEBIH KONSERVATIF.
    - Daging/protein (ayam, ikan, daging): porsi standar = 80-120g.
      Hanya melebihi 120g jika makanan jelas memenuhi lebih dari separuh piring makan.
    - Nasi putih matang: 1 centong standar = ~100-120g. Porsi sedang = 150-180g.
    - Mie/pasta matang: porsi sedang = 150-200g.
    - Sayur tumis/rebus: porsi standar = 50-80g.
    - Jika ragu antara dua estimasi berat, SELALU pilih yang lebih rendah.
    - Gunakan piring standar (diameter 25cm) atau sendok makan (15ml) sebagai acuan 
      ukuran jika terlihat dalam foto.

    Kembalikan HANYA JSON array, tidak ada teks lain:
    [{"name":"Nasi Putih","portionG":150,"caloriesPer100g":175,"proteinPer100g":3.1,"carbsPer100g":38.9,"fatPer100g":0.3}]

    Gunakan nama makanan dalam bahasa Indonesia.
    Jika tidak ada makanan terdeteksi, kembalikan array kosong: []
""".trimIndent()
```

### Validation Plan

Test with 10 foods of known weight (weigh before photographing):

| Food | Actual (g) | Before Fix Avg | After Fix Target |
|------|-----------|----------------|-----------------|
| Ayam goreng | 100 | ~130 | 95–110 |
| Nasi putih | 150 | ~195 | 140–165 |
| Tempe goreng | 50 | ~65 | 47–58 |
| Ikan bakar | 120 | ~155 | 110–130 |

Acceptance: average error < 15% after fix (was ~25–30% before).

---

## 5. Implementation Order (Recommended)

```
1. Prompt Fix (GeminiService.kt)          — 30 min, immediate impact, no schema change
2. Goal Presets Endpoint                  — 2h, minimal risk, no schema change
3. V19 Migration + UserFoodLibrary module — 4h, new module
4. Quick-log endpoint                     — 2h, depends on step 3
```

---

## 6. Mobile Integration Notes

### My Foods Tab (in Log Meal flow)
- Entry point: new tab alongside AI Scan and Barcode
- Call `GET /api/user-foods?sort=RECENTLY_USED`
- Save button on AI Result screen: calls `POST /api/user-foods` with scan result data + `imageUrl` from `AiScanResponse`
- Quick-add (+): bottom sheet → select meal type + portion → `POST /api/user-foods/{id}/log`

### Goal Presets Screen
- On Settings → Daily Goals: call `GET /api/goals/daily/presets`
- Show 3 preset cards; selecting one pre-fills manual input fields
- Save: call existing `PUT /api/goals/daily`
- If no body profile: show error state with link to complete profile

---

## 7. Related Files

| File | Change |
|------|--------|
| `GeminiService.kt` | Update `prompt` val |
| `DailyGoalController.kt` | Add `GET /presets` endpoint |
| `DailyGoalService.kt` | Add `getPresets()` method |
| `DailyGoalDto.kt` | Add `GoalPreset`, `GoalPresetsResponse` data classes |
| `V19__user_food_library.sql` | New migration |
| `userfood/` (new module) | Full new module |
| `docs/product/API_SPEC.md` | Add new endpoint docs |
