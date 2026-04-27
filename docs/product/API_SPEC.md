# API Specification — Calories Tracker

Base URL: `http://localhost:8080`  
Auth: `Authorization: Bearer <accessToken>` (kecuali endpoint public)

---

## Auth (dari template, sudah ada)

```
POST /api/auth/register      # Mobile self-register
POST /api/auth/login         # Login semua user
POST /api/auth/refresh       # Refresh access token
GET  /api/auth/me            # Profile sendiri
PUT  /api/auth/me            # Update profile + FCM token
```

---

## Body Profile

### GET `/api/profile/body`
Ambil body stats user yang sedang login.

**Response:**
```json
{
  "heightCm": 170,
  "weightKg": 75.5,
  "birthDate": "1995-06-15",
  "gender": "MALE",
  "activityLevel": "LIGHTLY_ACTIVE",
  "goal": "LOSE",
  "targetWeightKg": 68.0,
  "bmrKcal": 1780.5,
  "tdeeKcal": 2447.7,
  "recommendedCalories": 1947.7,
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

### POST `/api/profile/body`
Create atau update body profile. Otomatis hitung BMR/TDEE dan update `daily_goals`.

**Request:**
```json
{
  "heightCm": 170,
  "weightKg": 75.5,
  "birthDate": "1995-06-15",
  "gender": "MALE",
  "activityLevel": "LIGHTLY_ACTIVE",
  "goal": "LOSE",
  "targetWeightKg": 68.0
}
```

**Response:** body profile lengkap (sama dengan GET)

### GET `/api/profile/body/bmr-preview`
Preview kalkulasi BMR/TDEE tanpa menyimpan. Berguna untuk UI onboarding.

**Query params:** `heightCm`, `weightKg`, `birthDate`, `gender`, `activityLevel`, `goal`

**Response:**
```json
{
  "bmrKcal": 1780.5,
  "tdeeKcal": 2447.7,
  "recommendedCalories": 1947.7,
  "recommendedProteinG": 146.1,
  "recommendedCarbsG": 194.8,
  "recommendedFatG": 64.9
}
```

---

## Daily Goals

### GET `/api/goals/daily`
Ambil target kalori & makro hari ini.

**Response:**
```json
{
  "targetCalories": 1947.7,
  "targetProteinG": 146.1,
  "targetCarbsG": 194.8,
  "targetFatG": 64.9,
  "autoCalculated": true,
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

### PUT `/api/goals/daily`
Override target secara manual.

**Request:**
```json
{
  "targetCalories": 1800,
  "targetProteinG": 150,
  "targetCarbsG": 180,
  "targetFatG": 60
}
```

### POST `/api/goals/daily/reset`
Reset ke kalkulasi otomatis dari body profile.

---

## Food Database

### GET `/api/foods`
Search makanan (full-text search + filter).

**Query params:**
- `q` — keyword pencarian (name)
- `categoryId` — filter kategori
- `page` — halaman (default 0)
- `size` — ukuran halaman (default 20, max 50)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Nasi Putih",
      "categoryName": "Nasi & Sereal",
      "caloriesPer100g": 175,
      "proteinPer100g": 3.1,
      "carbsPer100g": 39.8,
      "fatPer100g": 0.2,
      "defaultServingG": 200,
      "servingDescription": "1 centong (200g)",
      "source": "TKPI",
      "isVerified": true
    }
  ],
  "totalElements": 500,
  "totalPages": 25,
  "page": 0,
  "size": 20
}
```

### GET `/api/foods/{id}`
Detail makanan by ID.

### GET `/api/foods/barcode/{barcode}`
Lookup by barcode. Urutan lookup:
1. Cek `food_items` lokal dulu
2. Jika tidak ada, fallback ke Open Food Facts API
3. Jika ketemu di OFF, simpan ke lokal sebagai cache (source=OPENFOODFACTS)
4. Return data

**Response:** sama dengan GET `/api/foods/{id}`, + field `source`

### POST `/api/foods/submit`
User submit makanan baru (masuk antrian review admin, `is_verified=false`).

**Request:**
```json
{
  "name": "Pecel Madiun",
  "categoryId": 10,
  "caloriesPer100g": 120,
  "proteinPer100g": 5.2,
  "carbsPer100g": 18.3,
  "fatPer100g": 3.1,
  "defaultServingG": 200,
  "servingDescription": "1 porsi (200g)"
}
```

---

## Meal Logs

### GET `/api/meal-logs`
Ambil diary untuk satu hari.

**Query params:** `date` (format `YYYY-MM-DD`, default today)

**Response:**
```json
{
  "date": "2024-01-15",
  "summary": {
    "totalCalories": 1250.5,
    "totalProteinG": 85.2,
    "totalCarbsG": 160.3,
    "totalFatG": 38.1,
    "targetCalories": 1947.7,
    "remainingCalories": 697.2,
    "caloriesPercent": 64
  },
  "meals": {
    "BREAKFAST": [
      {
        "id": 101,
        "foodName": "Nasi Putih",
        "quantityG": 200,
        "calories": 350,
        "proteinG": 6.2,
        "carbsG": 79.6,
        "fatG": 0.4,
        "aiScanPhotoUrl": null,
        "loggedAt": "2024-01-15"
      }
    ],
    "LUNCH": [],
    "DINNER": [],
    "SNACK": []
  }
}
```

### POST `/api/meal-logs`
Tambah entry log makanan.

**Request:**
```json
{
  "foodItemId": 1,
  "quantityG": 200,
  "mealType": "BREAKFAST",
  "loggedAt": "2024-01-15",
  "aiScanPhotoUrl": null
}
```

**Response:** meal log entry yang baru dibuat.

**Validasi subscription:**
- Free user: cek apakah sudah mencapai `free_daily_log_limit` untuk tanggal tersebut
- Jika sudah limit → `403 SUBSCRIPTION_LIMIT_REACHED`

### PUT `/api/meal-logs/{id}`
Edit log (quantity atau meal_type). Hanya bisa edit log milik sendiri.

**Request:**
```json
{
  "quantityG": 300,
  "mealType": "LUNCH"
}
```

### DELETE `/api/meal-logs/{id}`
Soft delete log.

### GET `/api/meal-logs/summary`
Summary kalori periode (untuk analytics screen).

**Query params:** `from` (YYYY-MM-DD), `to` (YYYY-MM-DD)

**Response:**
```json
{
  "days": [
    {
      "date": "2024-01-15",
      "totalCalories": 1250.5,
      "totalProteinG": 85.2,
      "totalCarbsG": 160.3,
      "totalFatG": 38.1
    }
  ],
  "averageCalories": 1456.8,
  "daysOnTarget": 3
}
```

**Akses:** Free tier hanya 1 hari, Premium 90 hari.

---

## AI Scan

### POST `/api/ai-scan/analyze`
Upload foto makanan → AI identifikasi.

**Request:** `multipart/form-data`
- `photo` — file gambar (JPG/PNG, max 5MB)
- `mealType` — BREAKFAST | LUNCH | DINNER | SNACK (optional, default SNACK)

**Processing:**
1. Upload foto ke MinIO, dapat URL
2. Kirim URL ke OpenAI Vision / Gemini Vision
3. Parse response AI → nama makanan + estimasi porsi
4. Lookup nama ke food_items (fuzzy match)
5. Return saran + confidence

**Response:**
```json
{
  "photoUrl": "https://storage.../scan/uuid.jpg",
  "suggestions": [
    {
      "foodItem": {
        "id": 5,
        "name": "Nasi Goreng",
        "caloriesPer100g": 182
      },
      "estimatedQuantityG": 250,
      "estimatedCalories": 455,
      "confidence": 0.87,
      "aiNote": "Terdeteksi: nasi goreng dengan telur dan sayuran"
    }
  ],
  "rawAiResponse": "Makanan: Nasi Goreng. Estimasi porsi: 250g."
}
```

**Error:**
- `403 PREMIUM_REQUIRED` — user free tier mencoba pakai AI scan
- `503 AI_SERVICE_UNAVAILABLE` — API OpenAI/Gemini down

### POST `/api/ai-scan/confirm`
Konfirmasi saran AI dan langsung log ke diary.

**Request:**
```json
{
  "photoUrl": "https://storage.../scan/uuid.jpg",
  "foodItemId": 5,
  "quantityG": 250,
  "mealType": "BREAKFAST",
  "loggedAt": "2024-01-15"
}
```

---

## Subscription

### GET `/api/subscription/current`
Status subscription user.

**Response:**
```json
{
  "plan": {
    "name": "FREE",
    "label": "Gratis",
    "aiScanEnabled": false,
    "maxLogsPerDay": 10,
    "macroHistoryDays": 1
  },
  "status": "ACTIVE",
  "expiresAt": null,
  "logsUsedToday": 4,
  "logsRemainingToday": 6
}
```

### GET `/api/subscription/plans`
Daftar plan yang tersedia.

**Response:**
```json
[
  {
    "name": "FREE",
    "label": "Gratis",
    "priceIdr": 0,
    "features": ["10 log/hari", "Barcode scan", "BMR calculator"]
  },
  {
    "name": "PREMIUM_MONTHLY",
    "label": "Premium Bulanan",
    "priceIdr": 49000,
    "durationDays": 30,
    "features": ["Unlimited log", "AI foto scan", "Riwayat 90 hari", "Export CSV"]
  },
  {
    "name": "PREMIUM_YEARLY",
    "label": "Premium Tahunan",
    "priceIdr": 399000,
    "durationDays": 365,
    "discountPercent": 32,
    "features": ["Unlimited log", "AI foto scan", "Riwayat 365 hari", "Export CSV"]
  }
]
```

### POST `/api/subscription/checkout`
Buat order pembayaran ke Midtrans.

**Request:**
```json
{
  "planName": "PREMIUM_MONTHLY"
}
```

**Response:**
```json
{
  "orderId": "SUB-20240115-UUID",
  "snapToken": "midtrans-snap-token",
  "redirectUrl": "https://app.midtrans.com/snap/v4/...",
  "expiresAt": "2024-01-15T11:00:00Z"
}
```

### POST `/api/subscription/webhook/midtrans`
Webhook callback dari Midtrans (public endpoint, verifikasi signature Midtrans).
Jika `transaction_status=settlement` → aktifkan subscription.

---

## Admin — Food Management

> Semua endpoint admin butuh role ADMIN atau SUPER_ADMIN

```
GET    /api/admin/foods                  # List + filter (unverified, by source)
GET    /api/admin/foods/{id}
POST   /api/admin/foods                  # Create makanan baru
PUT    /api/admin/foods/{id}             # Edit
DELETE /api/admin/foods/{id}             # Soft delete
POST   /api/admin/foods/{id}/verify      # Verifikasi submisi user
POST   /api/admin/foods/import-tkpi      # Import bulk dari CSV TKPI
GET    /api/admin/foods/pending-review   # Daftar submisi user yang belum diverifikasi
```

---

## Admin — Users & Subscriptions

```
GET  /api/admin/users                           # List users (dari template)
GET  /api/admin/users/{id}/subscription         # Subscription detail user
PUT  /api/admin/users/{id}/subscription/grant   # Grant premium manual (gratis/promo)
PUT  /api/admin/users/{id}/subscription/revoke  # Cabut premium
```

---

## Admin — Analytics

```
GET /api/admin/analytics/overview         # DAU/MAU, total log, revenue hari ini
GET /api/admin/analytics/revenue          # Revenue chart (by day/week/month)
GET /api/admin/analytics/top-foods        # Top 20 makanan paling sering di-log
GET /api/admin/analytics/user-retention   # Cohort retention chart
```

**Query params untuk analytics:** `period` (7d | 30d | 90d), `from`, `to`

---

## Error Codes

| Code | HTTP | Arti |
|---|---|---|
| `SUBSCRIPTION_LIMIT_REACHED` | 403 | Free user sudah melebihi limit log harian |
| `PREMIUM_REQUIRED` | 403 | Fitur hanya untuk Premium |
| `FOOD_NOT_FOUND` | 404 | Food item tidak ditemukan |
| `BARCODE_NOT_FOUND` | 404 | Barcode tidak ada di lokal maupun Open Food Facts |
| `AI_SERVICE_UNAVAILABLE` | 503 | OpenAI/Gemini API tidak tersedia |
| `PAYMENT_FAILED` | 402 | Transaksi pembayaran gagal |
| `INVALID_BODY_PROFILE` | 422 | Data body profile tidak valid |

---

## Pagination Standard

Semua endpoint list menggunakan format ini:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 500,
  "totalPages": 25,
  "first": true,
  "last": false
}
```
