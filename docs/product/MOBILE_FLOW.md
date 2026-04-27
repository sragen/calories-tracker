# Mobile App Flow — Calories Tracker

Stack: Compose Multiplatform + KMM (Android + iOS)

---

## Navigasi Utama

```
App Launch
    │
    ├── [Belum login] ──────────────────────▶ AuthNavGraph
    │                                          ├── WelcomeScreen
    │                                          ├── LoginScreen
    │                                          └── RegisterScreen
    │
    └── [Sudah login]
             │
             ├── [Body profile belum ada] ──▶ OnboardingNavGraph
             │                                ├── BodyProfileScreen
             │                                └── GoalSetupScreen
             │
             └── [Profile lengkap] ──────────▶ MainNavGraph (Bottom Nav)
                                               ├── HomeScreen (default)
                                               ├── DiaryScreen
                                               ├── ScanScreen (FAB)
                                               ├── SearchFoodScreen
                                               └── ProfileScreen
```

---

## Screen List

### Auth Screens

#### `WelcomeScreen`
- Ilustrasi + tagline
- Tombol: "Mulai Gratis" → Register, "Sudah punya akun" → Login

#### `RegisterScreen`
- Input: Nama, Email, Password, Konfirmasi Password
- Tombol: "Daftar" → POST /api/auth/register → LoginScreen (auto login)

#### `LoginScreen`
- Input: Email, Password
- Tombol: "Masuk" → POST /api/auth/login → cek body_profile

---

### Onboarding Screens (hanya muncul 1x setelah register)

#### `BodyProfileScreen`
Step 1 dari 2 — Input data fisik.

```
┌─────────────────────────────────┐
│  Kenalan dulu yuk! 👋           │
│  Kami perlu info ini untuk      │
│  menghitung kebutuhan kalorimu  │
│                                 │
│  Tinggi badan                   │
│  [  170  ] cm                   │
│                                 │
│  Berat badan                    │
│  [  75   ] kg                   │
│                                 │
│  Tanggal lahir                  │
│  [  15 Jun 1995  ] 📅           │
│                                 │
│  Jenis kelamin                  │
│  ● Pria   ○ Wanita              │
│                                 │
│  Tingkat aktivitas              │
│  ┌────────────────────────────┐ │
│  │ 🪑 Hampir tidak olahraga  │ │  (dropdown)
│  └────────────────────────────┘ │
│                                 │
│        [ Lanjut → ]             │
└─────────────────────────────────┘
```

**Pilihan aktivitas:**
- Hampir tidak olahraga (SEDENTARY)
- Olahraga ringan 1-3x/minggu (LIGHTLY_ACTIVE)
- Olahraga sedang 3-5x/minggu (MODERATELY_ACTIVE)
- Olahraga berat 6-7x/minggu (VERY_ACTIVE)
- Sangat aktif / pekerja fisik (EXTRA_ACTIVE)

#### `GoalSetupScreen`
Step 2 dari 2 — Target & tujuan.

```
┌─────────────────────────────────┐
│  Apa tujuanmu? 🎯               │
│                                 │
│  ○ Turunkan berat badan  ⬇️    │
│  ● Jaga berat sekarang   ⚖️    │
│  ○ Tambah berat badan    ⬆️    │
│                                 │
│  Target berat (opsional)        │
│  [  68   ] kg                   │
│                                 │
│  ─────────────────────────────  │
│  Berdasarkan data kamu:         │
│                                 │
│  BMR        : 1,780 kcal/hari   │
│  TDEE       : 2,448 kcal/hari   │
│  Target     : 1,948 kcal/hari   │
│                                 │
│  Protein: 146g | Karbo: 195g    │
│  Lemak: 65g                     │
│                                 │
│      [ Mulai Tracking! 🚀 ]     │
└─────────────────────────────────┘
```

---

### Main Screens

#### `HomeScreen`
Dashboard utama — ringkasan hari ini.

```
┌─────────────────────────────────┐
│ Sabtu, 15 Jan 2024     🔔 ⚙️   │
│                                 │
│      Kalori Hari Ini            │
│   ┌─────────────────────┐       │
│   │    ●●●●●●●○○○○      │       │
│   │      1,250          │       │
│   │   dari 1,948 kcal   │       │
│   │   sisa: 698 kcal    │       │
│   └─────────────────────┘       │
│                                 │
│  Makro                          │
│  Protein ██████░░  85/146g      │
│  Karbo   █████████ 160/195g     │
│  Lemak   ████░░░░  38/65g       │
│                                 │
│  Log Hari Ini                   │
│  ┌──────────────────────────┐   │
│  │ 🌅 Sarapan      350 kcal │   │
│  │   Nasi Putih 200g        │   │
│  ├──────────────────────────┤   │
│  │ ☀️ Makan Siang  — kcal  │   │
│  │   + Tambah makanan       │   │
│  ├──────────────────────────┤   │
│  │ 🌙 Makan Malam  — kcal  │   │
│  ├──────────────────────────┤   │
│  │ 🍪 Camilan       — kcal  │   │
│  └──────────────────────────┘   │
│                                 │
│  ════ Bottom Navigation ════   │
│  🏠 Home  📓 Diary  🔍 Search  │
│              📷 Scan  👤 Profile│
└─────────────────────────────────┘
```

**FAB utama:** Tombol kamera besar di tengah bottom nav → `ScanScreen`

#### `DiaryScreen`
Tampilan diary detail per hari + navigasi antar hari.

```
┌─────────────────────────────────┐
│  ← 14 Jan    15 Jan    16 Jan → │
│                                 │
│  Total: 1,250 / 1,948 kcal      │
│                                 │
│  🌅 SARAPAN          350 kcal   │
│  ┌──────────────────────────┐   │
│  │ Nasi Putih    200g  350  │ ✎ │
│  └──────────────────────────┘   │
│  [ + Tambah ke Sarapan ]        │
│                                 │
│  ☀️ MAKAN SIANG       — kcal   │
│  [ + Tambah ke Makan Siang ]    │
│                                 │
│  🌙 MAKAN MALAM       — kcal   │
│  [ + Tambah ke Makan Malam ]    │
│                                 │
│  🍪 CAMILAN           900 kcal  │
│  ┌──────────────────────────┐   │
│  │ Indomie Goreng 85g  360  │ ✎ │
│  │ Pisang Ambon  100g   99  │ ✎ │
│  └──────────────────────────┘   │
│  [ + Tambah Camilan ]           │
└─────────────────────────────────┘
```

#### `SearchFoodScreen`
Cari makanan untuk di-log.

```
┌─────────────────────────────────┐
│  ← Log untuk: Sarapan 🌅        │
│                                 │
│  [ 🔍 Cari makanan...        ]  │
│                                 │
│  Cara log:                      │
│  [📷 Foto]  [📊 Barcode]  [✏️ Ketik] │
│                                 │
│  Sering Dimakan:                │
│  ┌──────────────────────────┐   │
│  │ Nasi Putih       175/100g│ + │
│  │ Ayam Goreng      260/100g│ + │
│  │ Teh Manis         50/cup │ + │
│  └──────────────────────────┘   │
│                                 │
│  Hasil Pencarian "nasi":        │
│  ┌──────────────────────────┐   │
│  │ Nasi Putih       175 kcal│ + │
│  │ Nasi Goreng      182 kcal│ + │
│  │ Nasi Uduk        155 kcal│ + │
│  │ Nasi Kuning      180 kcal│ + │
│  └──────────────────────────┘   │
└─────────────────────────────────┘
```

#### `FoodDetailSheet`
Bottom sheet muncul saat user tap makanan di search.

```
┌─────────────────────────────────┐
│  Nasi Putih              ✕     │
│  175 kcal per 100g   ✓ Verified│
│                                 │
│  Porsi                          │
│  [ ─ ]  [ 200 ]  [ + ]  g      │
│  "1 centong (200g)"             │
│                                 │
│  Untuk waktu makan:             │
│  ○ Sarapan  ● Makan Siang       │
│  ○ Makan Malam  ○ Camilan       │
│                                 │
│  ─────────────────────────────  │
│  Nutrisi (200g):                │
│  Kalori    350 kcal             │
│  Protein   6.2 g                │
│  Karbohidrat 79.6 g             │
│  Lemak     0.4 g                │
│                                 │
│        [ + Tambah ke Log ]      │
└─────────────────────────────────┘
```

#### `ScanScreen`
AI foto scan makanan.

```
┌─────────────────────────────────┐
│              ✕                  │
│                                 │
│  ┌─────────────────────────┐    │
│  │                         │    │
│  │      [VIEWFINDER]       │    │
│  │                         │    │
│  │   📐 Arahkan kamera ke  │    │
│  │      makananmu          │    │
│  │                         │    │
│  └─────────────────────────┘    │
│                                 │
│  [📷 Ambil Foto]  [🖼️ Galeri]  │
│                                 │
│  ── atau ──                     │
│                                 │
│  [📊 Scan Barcode]              │
│                                 │
│  ⚡ Premium: AI Scan aktif       │
└─────────────────────────────────┘
```

#### `ScanResultScreen`
Setelah foto dianalisis AI.

```
┌─────────────────────────────────┐
│  ← Hasil Analisis AI            │
│                                 │
│  [FOTO MAKANAN]                 │
│                                 │
│  🤖 AI mendeteksi:              │
│  "Nasi Goreng dengan telur      │
│   dan sayuran"                  │
│                                 │
│  Saran:                         │
│  ┌──────────────────────────┐   │
│  │ ● Nasi Goreng            │   │
│  │   182 kcal/100g          │   │
│  │   Keyakinan: 87%         │   │
│  ├──────────────────────────┤   │
│  │ ○ Nasi Putih + Lauk      │   │
│  │   Bukan ini?             │   │
│  └──────────────────────────┘   │
│                                 │
│  Porsi:                         │
│  [ ─ ]  [ 250 ]  [ + ]  g       │
│                                 │
│  Untuk waktu makan: Sarapan     │
│                                 │
│  [ Tidak Tepat — Cari Manual ]  │
│  [    ✓ Konfirmasi & Log    ]   │
└─────────────────────────────────┘
```

#### `ProfileScreen`
Setting dan info user.

```
┌─────────────────────────────────┐
│  Profil                         │
│                                 │
│  [👤]  Budi Santoso             │
│        budi@email.com           │
│                                 │
│  Status: 🌟 Premium (s/d 14 Feb)│
│  [ Upgrade / Perpanjang ]       │
│                                 │
│  ─── Data Fisik ───             │
│  Tinggi      170 cm             │
│  Berat       75.5 kg    [Edit]  │
│  Tujuan      Turunkan BB        │
│  Target      1,948 kcal/hari    │
│                                 │
│  ─── Pengaturan ───             │
│  Notifikasi Pengingat Makan  🔔 │
│  Satuan berat                kg │
│                                 │
│  ─── Lainnya ───                │
│  Export data CSV                │
│  Kebijakan Privasi              │
│  Syarat & Ketentuan             │
│                                 │
│  [ Keluar ]                     │
└─────────────────────────────────┘
```

#### `SubscriptionScreen`
Tampilan upgrade premium.

```
┌─────────────────────────────────┐
│  ← Upgrade Premium              │
│                                 │
│  🌟 Buka Semua Fitur            │
│                                 │
│  ✅ AI Foto Scan tanpa batas    │
│  ✅ Log makanan unlimited       │
│  ✅ Riwayat hingga 90 hari      │
│  ✅ Export data CSV             │
│                                 │
│  ┌──────────────────────────┐   │
│  │     💎 PREMIUM TAHUNAN   │   │ ← Direkomendasikan
│  │     Rp 399.000/tahun     │   │
│  │     ≈ Rp 33.250/bulan    │   │
│  │     Hemat 32%! 🔥        │   │
│  └──────────────────────────┘   │
│                                 │
│  ┌──────────────────────────┐   │
│  │     Premium Bulanan      │   │
│  │     Rp 49.000/bulan      │   │
│  └──────────────────────────┘   │
│                                 │
│  [  Coba 7 Hari Gratis  ]       │
│                                 │
│  Bayar via QRIS, VA, Kartu      │
└─────────────────────────────────┘
```

---

## Navigation Graph (KMM)

```kotlin
// AppNavigation.kt

sealed class Route {
    // Auth
    object Welcome : Route()
    object Login : Route()
    object Register : Route()

    // Onboarding
    object BodyProfile : Route()
    object GoalSetup : Route()

    // Main
    object Home : Route()
    object Diary : Route()
    object Scan : Route()
    object ScanResult : Route()
    object SearchFood : Route()
    object FoodDetail : Route()
    object Profile : Route()
    object Subscription : Route()
    object BarcodeScanner : Route()
}
```

---

## State & ViewModel per Screen

| Screen | ViewModel | Key State |
|---|---|---|
| HomeScreen | HomeViewModel | DailySummary, MealSections |
| DiaryScreen | DiaryViewModel | SelectedDate, MealLogs |
| SearchFoodScreen | SearchFoodViewModel | Query, Results, RecentFoods |
| ScanScreen | ScanViewModel | CameraState |
| ScanResultScreen | ScanResultViewModel | AiSuggestions, SelectedFood |
| BodyProfileScreen | OnboardingViewModel | BodyProfile, BmrPreview |
| GoalSetupScreen | OnboardingViewModel | Goal, CalculatedMacros |
| ProfileScreen | ProfileViewModel | UserProfile, Subscription |
| SubscriptionScreen | SubscriptionViewModel | Plans, CurrentPlan |

---

## Shared KMM Layer

```
mobile/shared/
├── data/
│   ├── model/
│   │   ├── FoodItem.kt
│   │   ├── MealLog.kt
│   │   ├── BodyProfile.kt
│   │   ├── DailyGoal.kt
│   │   ├── DailySummary.kt
│   │   └── Subscription.kt
│   ├── network/
│   │   └── ApiService.kt          # Semua API call
│   └── repository/
│       ├── FoodRepository.kt
│       ├── MealLogRepository.kt
│       ├── BodyProfileRepository.kt
│       └── SubscriptionRepository.kt
└── storage/
    └── TokenStorage.kt            # Sudah ada di template
```
