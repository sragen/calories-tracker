# Integrations Design — Calories Tracker

Desain teknis integrasi dengan layanan eksternal.

---

## 1. AI Food Recognition

### Pilihan Provider

| Provider | Model | Kelebihan | Kekurangan |
|---|---|---|---|
| OpenAI Vision | `gpt-4o` | Akurasi tinggi, konteks baik | Lebih mahal ($0.01-0.03/foto) |
| Google Gemini | `gemini-2.0-flash` | Lebih murah, cepat | Akurasi sedikit lebih rendah |

**Rekomendasi:** Mulai dengan **Gemini Flash** (hemat biaya untuk MVP), bisa switch ke GPT-4o jika akurasi kurang.

### Prompt Engineering

```
System prompt:
"Kamu adalah ahli nutrisi yang mengidentifikasi makanan dari foto.
Jawab dalam Bahasa Indonesia.
Identifikasi semua makanan yang terlihat dan estimasi porsi dalam gram.
Jika ada beberapa komponen (misal: nasi + lauk + sayur), sebutkan masing-masing.
Format jawaban: JSON array."

User prompt (+ foto):
"Identifikasi makanan dalam foto ini dan estimasi porsinya."

Expected response:
[
  {
    "nama": "Nasi Putih",
    "estimasi_gram": 200,
    "confidence": 0.92
  },
  {
    "nama": "Ayam Goreng",
    "estimasi_gram": 80,
    "confidence": 0.88
  }
]
```

### Flow di Backend

```kotlin
// AiScanService.kt

suspend fun analyzeFood(photoUrl: String): List<AiSuggestion> {
    // 1. Kirim ke AI
    val aiResponse = geminiClient.analyzeImage(
        imageUrl = photoUrl,
        prompt = AI_FOOD_PROMPT
    )

    // 2. Parse JSON response
    val detectedFoods = parseAiResponse(aiResponse)

    // 3. Match ke food_items database (fuzzy search)
    return detectedFoods.map { detected ->
        val matched = foodRepository.searchByName(detected.nama).firstOrNull()
        AiSuggestion(
            foodItem = matched,
            estimatedQuantityG = detected.estimasiGram,
            confidence = detected.confidence,
            aiNote = "Terdeteksi: ${detected.nama}"
        )
    }
}
```

### Biaya Estimasi

Asumsi 1,000 user premium, masing-masing scan 3x/hari:

| Provider | Token/foto | Harga/token | Biaya/hari |
|---|---|---|---|
| Gemini Flash | ~500 tokens + gambar | $0.00015/1K | ~$22/hari |
| GPT-4o | ~500 tokens + gambar | $0.005/1K | ~$450/hari |

→ **Gemini Flash** jauh lebih ekonomis untuk MVP.

### Konfigurasi Backend

```yaml
# application.yml
ai:
  provider: GEMINI  # GEMINI | OPENAI
  gemini:
    api-key: ${GEMINI_API_KEY}
    model: gemini-2.0-flash
    max-tokens: 1000
    timeout-seconds: 30
  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-4o
```

---

## 2. Barcode Scan — Open Food Facts

### API

```
GET https://world.openfoodfacts.org/api/v2/product/{barcode}
```

Tidak butuh API key. Rate limit tidak ketat (komunitas open source).

### Flow

```kotlin
// BarcodeService.kt

suspend fun lookupBarcode(barcode: String): FoodItem? {
    // 1. Cek cache lokal dulu
    foodRepository.findByBarcode(barcode)?.let { return it }

    // 2. Fetch dari Open Food Facts
    val offProduct = openFoodFactsClient.getProduct(barcode)
        ?: return null  // produk tidak ditemukan

    // 3. Map ke FoodItem dan simpan sebagai cache
    val foodItem = FoodItem(
        name = offProduct.productName ?: offProduct.genericName,
        caloriesPer100g = offProduct.nutriments.energyKcal,
        proteinPer100g = offProduct.nutriments.proteins,
        carbsPer100g = offProduct.nutriments.carbohydrates,
        fatPer100g = offProduct.nutriments.fat,
        barcode = barcode,
        source = FoodSource.OPENFOODFACTS,
        externalId = offProduct.id,
        isVerified = false  // perlu review admin
    )

    return foodRepository.save(foodItem)
}
```

### Response Mapping dari Open Food Facts

```kotlin
// OFF API response structure yang penting
data class OpenFoodFactsProduct(
    val id: String,
    val productName: String?,
    val genericName: String?,
    val nutriments: OFFNutriments
)

data class OFFNutriments(
    @JsonProperty("energy-kcal_100g")
    val energyKcal: Double?,
    @JsonProperty("proteins_100g")
    val proteins: Double?,
    @JsonProperty("carbohydrates_100g")
    val carbohydrates: Double?,
    @JsonProperty("fat_100g")
    val fat: Double?,
    @JsonProperty("fiber_100g")
    val fiber: Double?
)
```

### Fallback jika Barcode Tidak Ditemukan

1. Return `404 BARCODE_NOT_FOUND`
2. Mobile menampilkan: "Produk tidak ditemukan. Tambahkan secara manual?"
3. User bisa isi data manual → POST `/api/foods/submit` (pending review)

---

## 3. File Storage — MinIO / S3

MinIO sudah ada di template (Docker Compose). Digunakan untuk foto scan AI.

### Konfigurasi

```yaml
# application-local.yml (sudah ada di template)
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket:
    food-scans: food-scan-photos  # Bucket baru untuk foto scan
    uploads: uploads               # Sudah ada
```

### Upload Flow

```kotlin
// AiScanController.kt

@PostMapping("/analyze", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
suspend fun analyzePhoto(@RequestPart photo: MultipartFile): AiScanResponse {
    // 1. Validasi: premium user only
    val user = currentUser()
    subscriptionService.requirePremium(user.id)

    // 2. Validasi file
    require(photo.size <= 5_000_000) { "Ukuran foto maksimal 5MB" }
    require(photo.contentType in listOf("image/jpeg", "image/png", "image/webp"))

    // 3. Upload ke MinIO
    val objectName = "scan/${user.id}/${UUID.randomUUID()}.jpg"
    val photoUrl = minioService.upload(
        bucket = "food-scan-photos",
        objectName = objectName,
        data = photo.inputStream,
        contentType = photo.contentType
    )

    // 4. Kirim URL ke AI
    val suggestions = aiScanService.analyzeFood(photoUrl)

    return AiScanResponse(photoUrl = photoUrl, suggestions = suggestions)
}
```

### Retention Policy

Foto scan disimpan 30 hari (hemat storage). Bisa dikonfigurasi via MinIO lifecycle policy.

---

## 4. Payment Gateway — Midtrans

### Flow Pembayaran

```
Mobile App                    Backend                    Midtrans
    │                            │                           │
    │ POST /subscription/checkout│                           │
    │──────────────────────────▶ │                           │
    │                            │ Create Transaction        │
    │                            │──────────────────────────▶│
    │                            │ Return snap_token         │
    │                            │◀──────────────────────────│
    │ Return snap_token          │                           │
    │◀────────────────────────── │                           │
    │                            │                           │
    │ Open Midtrans Snap UI      │                           │
    │ (WebView / in-app)         │                           │
    │                            │                           │
    │ User bayar via QRIS/VA     │                           │
    │                            │ POST /webhook/midtrans    │
    │                            │◀──────────────────────────│
    │                            │ Verifikasi signature      │
    │                            │ Aktifkan subscription     │
    │                            │                           │
    │ Push notification: "Premium aktif!" ◀─────────────────│
```

### Konfigurasi Backend

```yaml
# application.yml
midtrans:
  server-key: ${MIDTRANS_SERVER_KEY}
  client-key: ${MIDTRANS_CLIENT_KEY}
  is-production: false  # true untuk production
  base-url: https://app.sandbox.midtrans.com  # sandbox
```

### Create Transaction

```kotlin
// SubscriptionService.kt

suspend fun createCheckout(userId: Long, planName: String): CheckoutResponse {
    val plan = subscriptionPlanRepository.findByName(planName)
    val user = userRepository.findById(userId)
    val orderId = "SUB-${System.currentTimeMillis()}-${userId}"

    val snapToken = midtransClient.createTransaction(
        MidtransRequest(
            transactionDetails = TransactionDetails(
                orderId = orderId,
                grossAmount = plan.priceIdr.toLong()
            ),
            customerDetails = CustomerDetails(
                firstName = user.name,
                email = user.email
            ),
            itemDetails = listOf(
                ItemDetail(
                    id = plan.name,
                    name = plan.label,
                    price = plan.priceIdr.toLong(),
                    quantity = 1
                )
            ),
            expiry = Expiry(duration = 24, unit = "hours")
        )
    )

    // Simpan pending subscription
    subscriptionRepository.save(
        Subscription(
            userId = userId,
            planName = planName,
            status = SubscriptionStatus.PENDING_PAYMENT,
            paymentRef = orderId
        )
    )

    return CheckoutResponse(
        orderId = orderId,
        snapToken = snapToken.token,
        redirectUrl = snapToken.redirectUrl
    )
}
```

### Webhook Handler

```kotlin
// SubscriptionController.kt

@PostMapping("/webhook/midtrans")
fun handleMidtransWebhook(
    @RequestBody payload: MidtransWebhookPayload,
    @RequestHeader("X-Signature-Key") signature: String
): ResponseEntity<Unit> {
    // 1. Verifikasi signature
    val expectedSignature = sha512("${payload.orderId}${payload.statusCode}${payload.grossAmount}${midtransServerKey}")
    require(signature == expectedSignature) { "Invalid signature" }

    // 2. Handle berdasarkan status
    when (payload.transactionStatus) {
        "settlement", "capture" -> {
            subscriptionService.activateSubscription(payload.orderId)
            // Kirim push notification via FCM
            notificationService.sendPushNotification(
                userId = getUserIdFromOrderId(payload.orderId),
                title = "Premium aktif! 🌟",
                body = "Selamat! Fitur premium sudah bisa digunakan."
            )
        }
        "expire", "cancel" -> {
            subscriptionService.cancelSubscription(payload.orderId)
        }
    }

    return ResponseEntity.ok().build()
}
```

### Metode Pembayaran yang Diaktifkan

```kotlin
// Hanya aktifkan yang populer di Indonesia
enabledPayments = listOf(
    "gopay",
    "shopeepay",
    "qris",
    "bank_transfer",      // BCA, BNI, BRI, Mandiri, Permata
    "credit_card"
)
```

---

## Environment Variables

```bash
# .env / application-local.yml

# AI
GEMINI_API_KEY=AIza...
OPENAI_API_KEY=sk-...   # opsional, jika switch ke OpenAI

# Payment
MIDTRANS_SERVER_KEY=SB-Mid-server-...
MIDTRANS_CLIENT_KEY=SB-Mid-client-...

# MinIO (sudah ada di template)
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# PostgreSQL (sudah ada di template)
DB_URL=jdbc:postgresql://localhost:5433/calories_tracker_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

---

## Error Handling per Integrasi

### AI Service Down

```kotlin
// Circuit breaker: jika 3 request berturut gagal, fallback 5 menit
@CircuitBreaker(name = "aiService", fallbackMethod = "aiServiceFallback")
suspend fun analyzeFood(photoUrl: String): List<AiSuggestion> { ... }

suspend fun aiServiceFallback(photoUrl: String, ex: Exception): List<AiSuggestion> {
    log.warn("AI service unavailable: ${ex.message}")
    return emptyList()  // Return empty, mobile tampilkan "AI tidak tersedia, cari manual"
}
```

### Open Food Facts Timeout

```kotlin
// Timeout 5 detik, jika gagal return null (mobile fallback ke manual input)
withTimeout(5_000) {
    openFoodFactsClient.getProduct(barcode)
}
```

### Midtrans Webhook Retry

Midtrans retry webhook hingga 5x jika response bukan 200. Backend harus **idempotent** — cek `payment_ref` sebelum proses.
