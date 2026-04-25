# Panduan Membuat Produk Baru

Checklist lengkap dari clone sampai app siap dikembangkan.

---

## Checklist

### Fase 1 — Clone & Setup (15 menit)

- [ ] Clone repo template
  ```bash
  git clone <template-repo-url> nama-produk-baru
  cd nama-produk-baru
  rm -rf .git
  git init && git add . && git commit -m "initial: from app-template"
  ```

- [ ] Ganti nama produk di `backend/src/main/resources/application.yml`
  ```yaml
  app:
    name: "Nama Produk Baru"
  ```

- [ ] Ganti nama database di `backend/src/main/resources/application-local.yml`
  ```yaml
  spring:
    datasource:
      url: jdbc:postgresql://localhost:5432/nama_produk_db
  ```

- [ ] Ganti nama app di admin panel
  ```typescript
  // admin/src/config/app.config.ts
  export const appConfig = {
    name: "Nama Produk Baru",
    apiUrl: "http://localhost:8080",
  }
  ```

- [ ] Ganti Bundle ID Android di `mobile/androidApp/build.gradle.kts`
  ```kotlin
  applicationId = "com.company.namaprodu"
  ```

- [ ] Ganti Bundle ID iOS di Xcode → `iosApp` target → Signing & Capabilities
  ```
  Bundle Identifier: com.company.namaprodu
  ```

- [ ] Jalankan infrastruktur dan verifikasi
  ```bash
  docker-compose up -d
  cd backend && ./gradlew bootRun
  # Swagger UI harus bisa diakses
  ```

---

### Fase 2 — Branding (30 menit)

- [ ] Ganti warna brand di mobile
  ```kotlin
  // mobile/composeApp/src/commonMain/.../theme/Color.kt
  val PrimaryColor = Color(0xFF______) // masukkan hex warna brand
  val SecondaryColor = Color(0xFF______)
  ```

- [ ] Taruh logo di `admin/public/logo.svg`

- [ ] Ganti app icon Android
  - Gunakan Android Studio: File → New → Image Asset
  - Pilih `ic_launcher` sebagai nama

- [ ] Ganti app icon iOS
  - Buka Assets.xcassets di Xcode
  - Ganti semua ukuran di AppIcon

- [ ] Ganti nama app di `mobile/androidApp/src/main/res/values/strings.xml`
  ```xml
  <string name="app_name">Nama Produk Baru</string>
  ```

- [ ] Ganti nama app di `mobile/iosApp/iosApp/Info.plist`
  ```xml
  <key>CFBundleDisplayName</key>
  <string>Nama Produk Baru</string>
  ```

---

### Fase 3 — Domain Module Pertama (per modul: ~30 menit)

Untuk setiap domain bisnis produk baru (contoh: `Product`, `Order`, `Customer`):

**Backend:**
- [ ] Buat entity di `backend/src/main/kotlin/.../modules/namamodule/`
  - `NamaModule.kt` (Entity)
  - `NamaModuleDto.kt` (Request/Response DTO)
  - `NamaModuleRepository.kt` (extends BaseRepository)
  - `NamaModuleService.kt` (extends BaseService)
  - `NamaModuleController.kt` (extends BaseController)
- [ ] Buat migration SQL di `backend/src/main/resources/db/migration/`

**Admin Panel:**
- [ ] Tambah entry di `admin/src/config/nav.config.ts`
- [ ] Buat folder `admin/src/app/(dashboard)/namamodule/`
  - `columns.tsx`
  - `page.tsx` (list)
  - `create/page.tsx`
  - `[id]/edit/page.tsx`

**Mobile:**
- [ ] Tambah model di `mobile/shared-kmm/.../domain/model/`
- [ ] Tambah endpoint di `ApiService.kt`
- [ ] Buat Repository dan ViewModel
- [ ] Buat screen di `mobile/composeApp/.../screens/`
- [ ] Daftarkan route di `AppNavigation.kt`

---

### Fase 4 — Hapus Contoh (5 menit)

Setelah modul pertama jalan, hapus contoh dari template:

- [ ] Hapus `backend/.../modules/user/` jika tidak dibutuhkan
- [ ] Hapus `admin/src/app/(dashboard)/users/` jika tidak dibutuhkan
- [ ] Hapus screen contoh di mobile yang tidak relevan
- [ ] Bersihkan migration V1 jika struktur users berubah

---

## Naming Convention

| Konteks | Konvensi | Contoh |
|---|---|---|
| Kotlin class | PascalCase | `ProductService`, `OrderController` |
| Kotlin function/variable | camelCase | `findActiveProducts()` |
| Database table | snake_case | `products`, `order_items` |
| API endpoint | kebab-case | `/api/order-items` |
| TypeScript component | PascalCase | `ProductCard`, `DataTable` |
| TypeScript function/variable | camelCase | `useResource()`, `apiClient` |
| Compose function | PascalCase | `ProductListScreen()` |

---

## Arsitektur per Layer

### Backend: Module independen

Setiap domain module di backend berdiri sendiri. Tidak ada dependency antar module — jika butuh data dari module lain, call via service interface, bukan inject repository langsung.

```
✓ ProductService inject OrderRepository? → TIDAK
✓ ProductService inject OrderService?    → IYA (jika terpaksa)
✓ ProductService inject ProductRepository → IYA
```

### Mobile: Shared-first

Business logic dan data fetching selalu di `shared-kmm`. UI di `composeApp`.

```
✓ Logic di ViewModel (shared-kmm)        → IYA
✓ Logic di Screen composable (composeApp) → TIDAK
✓ Android-specific API di androidMain     → IYA
```

### Admin: Config-driven navigation

Sidebar terbentuk dari `nav.config.ts`. Tidak ada routing hardcode di komponen.

---

## Git Workflow per Produk

Setelah clone, setiap produk adalah repo mandiri:

```bash
# Setup remote baru untuk produk ini
git remote add origin <repo-produk-baru-url>
git push -u origin main

# Branch strategy
main          → production-ready code
develop       → integration branch
feature/*     → fitur baru
fix/*         → bug fixes
```

---

## Deployment Minimal

```bash
# Backend: Docker image
cd backend
docker build -t nama-produk-backend:latest .
# Deploy ke Railway / Fly.io / VPS dengan Docker

# Admin Panel: Static export
cd admin
npm run build
# Upload folder .next/ ke Vercel atau server Node.js

# Mobile: Build release
cd mobile
./gradlew :androidApp:assembleRelease   # Android APK
# iOS: Archive di Xcode → upload ke TestFlight
```
