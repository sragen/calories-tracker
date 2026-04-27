# Implementation Roadmap — Calories Tracker

Phase-by-phase plan dengan output terukur per fase.

---

## Gambaran Besar

```
Phase 1 (3–4 minggu)   Phase 2 (2–3 minggu)   Phase 3 (3–4 minggu)   Phase 4 (2 minggu)
Foundation             Scan & Discovery        AI & Premium           Polish & Admin
─────────────────────  ─────────────────────  ─────────────────────  ─────────────────
✓ Body profile          ✓ Barcode scan          ✓ AI foto scan          ✓ Analytics admin
✓ BMR/TDEE calc         ✓ Open Food Facts        ✓ Subscription flow     ✓ User management
✓ Manual food log       ✓ Food DB admin          ✓ Payment Midtrans      ✓ Feature flags
✓ Daily summary         ✓ Food categories        ✓ Free tier limits      ✓ Push notif
✓ TKPI seed data        ✓ Food search            ✓ Premium gating        ✓ Export CSV
✓ Auth + onboarding     ✓ Recent foods                                   ✓ App polish
```

---

## Phase 1 — Foundation

**Durasi:** 3–4 minggu  
**Output:** MVP yang bisa digunakan untuk log makanan manual + lihat kalori harian

### Backend

```
Week 1:
[ ] Setup database migrations V4-V8 (categories, food_items, body_profiles, daily_goals, meal_logs)
[ ] Seed TKPI data: 20 makanan populer Indonesia (testing)
[ ] Module body-profile: POST/GET /api/profile/body + kalkulasi BMR/TDEE
[ ] Module daily-goal: GET/PUT /api/goals/daily

Week 2:
[ ] Module food: GET /api/foods (search) + GET /api/foods/{id}
[ ] Module meal-log: CRUD /api/meal-logs + GET dengan daily summary
[ ] Validasi: user hanya bisa akses log sendiri
[ ] Unit test: BMR/TDEE calculation, nutrisi snapshot

Week 3:
[ ] Seed TKPI lengkap: 500 makanan Indonesia
[ ] GET /api/meal-logs/summary (periode) — untuk Phase 3 analytics
[ ] GET /api/profile/body/bmr-preview (tanpa simpan)
[ ] API testing + Swagger docs lengkap
```

### Mobile (Compose Multiplatform)

```
Week 1-2:
[ ] KMM: FoodItem, MealLog, BodyProfile, DailyGoal, DailySummary models
[ ] KMM: ApiService + HttpClient setup (sudah ada pola di template)
[ ] KMM: FoodRepository, MealLogRepository, BodyProfileRepository
[ ] WelcomeScreen, LoginScreen, RegisterScreen (sudah ada pola di template)

Week 2-3:
[ ] BodyProfileScreen (onboarding step 1)
[ ] GoalSetupScreen (onboarding step 2) + tampilkan preview BMR/TDEE
[ ] HomeScreen: calorie ring + macro bars + meal sections
[ ] SearchFoodScreen: search + hasil + recent foods
[ ] FoodDetailSheet: set porsi + pilih meal type
[ ] DiaryScreen: navigasi per hari

Week 3-4:
[ ] ProfileScreen: body stats, edit data
[ ] Navigation flow lengkap + kondisi auth check
[ ] Error states + loading states
[ ] Integration test: onboarding → log makanan → lihat di diary
```

### Admin Panel

```
Week 3-4:
[ ] Setup nav: Dashboard (placeholder), Foods, Users, Configs
[ ] /foods: List food items + search + filter
[ ] /foods/create dan /foods/[id]/edit: Form CRUD makanan
[ ] Dashboard: 4 stats card (placeholder data dulu)
```

**Definition of Done Phase 1:**
- User bisa register → isi body profile → lihat target kalori → cari makanan → log ke diary → lihat daily summary
- Admin bisa CRUD makanan di admin panel
- 500 makanan TKPI Indonesia bisa dicari dan di-log

---

## Phase 2 — Scan & Discovery

**Durasi:** 2–3 minggu  
**Output:** Barcode scan berjalan + food database lebih lengkap + admin bisa kelola

### Backend

```
Week 5:
[ ] GET /api/foods/barcode/{barcode}: lookup lokal → fallback Open Food Facts
[ ] OpenFoodFacts HTTP client (Ktor/OkHttp)
[ ] Auto-cache produk OFF ke food_items (source=OPENFOODFACTS)
[ ] POST /api/foods/submit: submisi makanan baru dari user (pending)

Week 6:
[ ] GET /api/admin/foods/pending-review
[ ] POST /api/admin/foods/{id}/verify + reject
[ ] POST /api/admin/foods/import-tkpi: bulk import CSV
[ ] Food categories endpoint: GET /api/foods/categories
```

### Mobile

```
Week 5:
[ ] BarcodeScannerScreen: kamera + ZXing scanner
[ ] Barcode scan → GET /api/foods/barcode → FoodDetailSheet
[ ] Fallback: "Produk tidak ditemukan" → form manual submit

Week 6:
[ ] Filter kategori di SearchFoodScreen
[ ] Recent foods: simpan 10 terakhir di local storage (SQLDelight)
[ ] Frequent foods: tampilkan di home berdasarkan paling sering di-log
[ ] Loading + error state untuk barcode lookup
```

### Admin Panel

```
Week 5-6:
[ ] /foods/pending: halaman review submisi user
[ ] Verifikasi / tolak submisi dengan 1 klik
[ ] Import CSV TKPI: upload file → preview → konfirmasi import
[ ] Filter food list by: source, category, verified status
```

**Definition of Done Phase 2:**
- User scan barcode kemasan produk → data nutrisi muncul otomatis
- User bisa submit makanan baru → admin review → approved
- Admin bisa import data massal dari CSV

---

## Phase 3 — AI & Premium

**Durasi:** 3–4 minggu  
**Output:** AI foto scan berjalan + subscription system + payment

### Backend

```
Week 7:
[ ] MinIO bucket setup untuk foto scan
[ ] AiScanService: upload foto → Gemini Vision → parse response → match food DB
[ ] POST /api/ai-scan/analyze + POST /api/ai-scan/confirm
[ ] Gemini Flash HTTP client

Week 8:
[ ] Subscription plans + V9, V10 migrations
[ ] GET /api/subscription/current + plans
[ ] POST /api/subscription/checkout: buat Midtrans transaction
[ ] POST /api/subscription/webhook/midtrans: handle callback

Week 9:
[ ] Subscription gating: middleware cek plan sebelum AI scan
[ ] Free tier limit: cek daily log count sebelum POST /api/meal-logs
[ ] Feature flag: ai_scan_enabled + free_daily_log_limit dari app_configs
[ ] Jadwal: cron job expire subscriptions harian
```

### Mobile

```
Week 7:
[ ] ScanScreen: camera capture + galleri picker
[ ] Upload foto ke backend → loading state → ScanResultScreen
[ ] ScanResultScreen: tampilkan saran AI + confidence
[ ] Konfirmasi saran → POST /api/ai-scan/confirm → log masuk diary

Week 8:
[ ] SubscriptionScreen: tampilkan plans + fitur
[ ] Checkout: buka Midtrans Snap via WebView / CustomTabs
[ ] Deep link callback setelah pembayaran → refresh subscription status
[ ] Free tier: tampilkan banner "0 scan tersisa" + CTA upgrade

Week 9:
[ ] Gate fitur AI scan: cek subscription sebelum buka kamera
[ ] Gate log: tampilkan dialog limit saat melebihi free tier
[ ] Profile: tampilkan status subscription + tanggal expired
[ ] Push notification saat subscription aktif (via FCM)
```

### Admin Panel

```
Week 8-9:
[ ] /users: tambah kolom subscription status
[ ] /users/[id]/subscription: detail + grant/revoke premium manual
[ ] /subscriptions: list semua transaksi + status
[ ] Config: toggle ai_scan_enabled + edit free_daily_log_limit
```

**Definition of Done Phase 3:**
- User foto makanan → AI identifikasi → konfirmasi → masuk diary
- User bisa berlangganan premium via QRIS/VA
- Pembayaran berhasil → fitur AI scan langsung terbuka

---

## Phase 4 — Polish & Admin Analytics

**Durasi:** 2 minggu  
**Output:** Admin analytics lengkap + app siap launch

### Backend

```
Week 10:
[ ] GET /api/admin/analytics/overview (DAU, MAU, total log, revenue)
[ ] GET /api/admin/analytics/revenue (chart data harian)
[ ] GET /api/admin/analytics/top-foods (20 teratas)
[ ] GET /api/meal-logs/summary (multi-day, batas premium 90 hari)
[ ] POST /api/meal-logs/export: export CSV (premium only)
```

### Mobile

```
Week 10-11:
[ ] AnalyticsScreen: weekly calorie chart + macro averages (Premium)
[ ] Export CSV dari ProfileScreen (Premium)
[ ] Notifikasi pengingat makan (FCM scheduled / local notification)
[ ] Animasi & transisi halus
[ ] Onboarding tips: "Sudah 3 hari berturut-turut!" achievement kecil
[ ] Error handling komprehensif (offline mode)
[ ] App icon + splash screen final
```

### Admin Panel

```
Week 11:
[ ] /analytics: dashboard chart lengkap (revenue, DAU/MAU, top foods, retention)
[ ] Dashboard cards: data real dari API
[ ] Export analytics data ke CSV (admin)
[ ] User search + filter di /users
```

**Definition of Done Phase 4 (Launch Ready):**
- Admin analytics dashboard berjalan dengan data real
- App bisa dijalankan offline untuk view diary (cached)
- App icon, splash screen, dan onboarding final
- Semua error states handled dengan pesan yang jelas

---

## Tech Debt & Perlu Diperhatikan

### Security
- [ ] Rate limiting untuk endpoint AI scan (max 20/hari free, unlimited premium)
- [ ] Validasi file upload: hanya JPG/PNG/WebP, max 5MB
- [ ] Midtrans webhook signature verifikasi wajib sebelum proses

### Performance
- [ ] Index PostgreSQL untuk `meal_logs(user_id, logged_at)` — query paling sering
- [ ] Cache hasil barcode lookup 24 jam (jangan query OFF tiap saat)
- [ ] Pagination wajib untuk semua list endpoint

### Data Quality
- [ ] Admin wajib review semua OPENFOODFACTS entry sebelum `is_verified=true`
- [ ] Seed data TKPI perlu diverifikasi akurasi kalorinya
- [ ] Backup database harian sejak day 1

---

## Checklist Go-Live

```
Backend:
[ ] Semua migrations berjalan bersih di production DB
[ ] Environment variables production terset (tidak ada localhost)
[ ] Midtrans production key (bukan sandbox)
[ ] Gemini / OpenAI API key production
[ ] MinIO/S3 bucket production dengan lifecycle policy
[ ] HTTPS enabled + CORS production domain
[ ] Backup database scheduled

Admin Panel:
[ ] Deploy ke Vercel / server
[ ] NEXT_PUBLIC_API_URL ke production backend URL
[ ] Login superadmin credentials diganti dari default

Mobile:
[ ] API_BASE_URL production di build config
[ ] Bundle ID final (com.innovareborndiesel.caloriestracker)
[ ] App signed dengan release keystore
[ ] Google Play Console: listing + screenshot + deskripsi
[ ] App Store Connect: listing + screenshot (iOS)
[ ] FCM Google Services JSON/plist production
[ ] Midtrans client key production
```
