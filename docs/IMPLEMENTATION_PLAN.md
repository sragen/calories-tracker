# Implementation Plan

Roadmap membangun template dari nol sampai running dan siap di-clone untuk produk baru.

**Target akhir:** Template yang bisa di-clone, ganti branding 5 menit, langsung punya:
- Backend jalan dengan auth + RBAC + feature flag
- Admin panel bisa login, manage users, toggle config
- Mobile app bisa launch, fetch config, dan login

---

## Overview Phases

```
Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5 ──► Phase 6
Backend      RBAC +      Feature     Admin       Mobile      Integration
Foundation   User Mgmt   Flag        Panel       App         & Finalize

[BACKEND DONE]────────────────────────►[ADMIN DONE]
                                                  │
                                       [MOBILE DONE]
                                                  │
                                         [TEMPLATE READY ✅]
```

---

## Phase 1 — Backend Foundation

**Estimasi:** 2-3 hari  
**Tujuan:** API jalan, database terhubung, bisa register dan login.

### Tasks

- [ ] Setup project Spring Boot 3.x (Kotlin, Gradle Kotlin DSL)
  - Dependencies: Spring Web, Spring Security, Spring Data JPA, Flyway, OpenAPI
  - Package structure: `com.company.app`

- [ ] Docker Compose untuk local dev
  - PostgreSQL 16
  - MinIO (object storage)
  - File: `docker-compose.yml` di root monorepo

- [ ] Konfigurasi dasar
  - `application.yml` (default config)
  - `application-local.yml.example` (template untuk developer)
  - `CorsConfig.kt`
  - `OpenApiConfig.kt` → Swagger UI di `/swagger-ui.html`

- [ ] Flyway Migrations
  - `V1__init_core.sql` → tabel `users`, `roles`, `refresh_tokens`, `uploaded_files`
  - `V2__role_permissions.sql` → tabel `role_permissions` + seed default
  - `V3__app_configs.sql` → tabel `app_configs` + seed 6 config default
  - Seed SUPER_ADMIN user

- [ ] Base classes (Generic CRUD)
  - `BaseEntity.kt` → `id`, `createdAt`, `updatedAt`, `deletedAt`
  - `BaseRepository.kt` → JpaRepository + `findAllActive()`
  - `BaseService.kt` → `findAll()`, `findById()`, `create()`, `update()`, `delete()`
  - `BaseController.kt` → REST endpoints GET/POST/PUT/DELETE

- [ ] Auth module
  - `JwtService.kt` → generate + validate token
  - `JwtFilter.kt` → Spring Security filter
  - `SecurityConfig.kt` → rules: public routes vs protected routes
  - `AuthController.kt` → `/api/auth/register`, `/login`, `/refresh`, `/me`
  - `GlobalExceptionHandler.kt` → format error response konsisten

- [ ] Health check endpoint
  - `GET /actuator/health` → `{"status":"UP"}`

### Acceptance Criteria ✅

```bash
# Semua ini harus berhasil sebelum lanjut ke Phase 2

curl http://localhost:8080/actuator/health
# → {"status":"UP"}

curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password","name":"Test"}'
# → {"id":1,"email":"test@test.com","role":"USER"}

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password"}'
# → {"accessToken":"eyJ...","refreshToken":"...","expiresIn":86400}

# Swagger UI accessible
open http://localhost:8080/swagger-ui.html
```

---

## Phase 2 — RBAC + User Management

**Estimasi:** 2 hari  
**Dependency:** Phase 1 selesai  
**Tujuan:** Permission system berfungsi. Admin bisa manage users.

### Tasks

- [ ] RBAC core
  - `Permission.kt` → enum: `READ`, `WRITE`, `DELETE`
  - `RequiresPermission.kt` → custom annotation
  - `PermissionAspect.kt` → Spring AOP intercept + check DB
  - `PermissionRepository.kt` → query `role_permissions` table
  - `PermissionService.kt` → `hasPermission(userId, module, action)`

- [ ] Update SecurityConfig
  - Route `/api/admin/**` → butuh role ADMIN atau SUPER_ADMIN
  - Route `/api/auth/**` → public
  - Route `/api/config` → public
  - Route lainnya → authenticated

- [ ] User Management module (`modules/user/`)
  - `User.kt` → entity dengan `role`, `status`, `fcmToken`
  - `UserDto.kt` → request/response DTOs
  - `UserRepository.kt`
  - `UserService.kt` → include `updateStatus()`, `updateRole()`
  - `AdminUserController.kt` → endpoints `/api/admin/users/**`

- [ ] Update AuthController
  - `PUT /api/auth/me` → update profile + fcmToken
  - Validasi: user INACTIVE/SUSPENDED → return 403 saat login

### Acceptance Criteria ✅

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"superadmin@company.com","password":"admin123"}' \
  | jq -r '.accessToken')

# List users (butuh ADMIN role)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/users
# → {"content":[...],"totalElements":2}

# Endpoint yang butuh permission DELETE ditolak untuk ADMIN biasa
# → 403 {"error":"FORBIDDEN","message":"Insufficient permission: USERS:DELETE"}
```

---

## Phase 3 — Feature Flag & Remote Config

**Estimasi:** 1 hari  
**Dependency:** Phase 1 selesai  
**Tujuan:** Mobile bisa fetch config. Admin bisa toggle flag.

### Tasks

- [ ] AppConfig module (`modules/config/`)
  - `AppConfig.kt` → entity (key, value, type, label, description, isActive)
  - `AppConfigRepository.kt`
  - `AppConfigService.kt` → `getAllActive()`, `update(key, value)`
  - `AppConfigController.kt`
    - `GET /api/config` → public, return flat JSON semua config aktif
    - `GET /api/admin/config` → ADMIN, list lengkap
    - `PUT /api/admin/config/{key}` → ADMIN, update value/toggle

- [ ] Response format `/api/config`
  ```json
  {
    "maintenance_mode": false,
    "min_app_version": "1.0.0",
    "force_update": false,
    "push_notification": true,
    "promo_banner_url": "",
    "max_retry_login": 5
  }
  ```

### Acceptance Criteria ✅

```bash
# Public endpoint (tidak butuh auth)
curl http://localhost:8080/api/config
# → {"maintenance_mode":false,"min_app_version":"1.0.0",...}

# Toggle maintenance mode via admin
curl -X PUT -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"value":"true"}' \
  http://localhost:8080/api/admin/config/maintenance_mode

# Verify
curl http://localhost:8080/api/config | jq '.maintenance_mode'
# → true
```

---

## Phase 4 — Admin Panel

**Estimasi:** 3-4 hari  
**Dependency:** Phase 1, 2, 3 selesai  
**Tujuan:** Admin panel bisa login, lihat user list, toggle feature flag.

### Tasks

- [ ] Setup Next.js 15 project
  - TypeScript, App Router, Tailwind v4
  - Install: Shadcn/UI, Tanstack Table, TanStack Query, React Hook Form, Zod, Axios

- [ ] Konfigurasi
  - `config/app.config.ts` → `appName`, `apiUrl`
  - `config/nav.config.ts` → menu sidebar awal: Users, Config
  - `lib/api-client.ts` → Axios instance + auth interceptor (attach JWT dari cookie)

- [ ] Auth flow
  - `app/(auth)/login/page.tsx` → form login
  - `lib/auth.ts` → simpan/hapus token di httpOnly cookie
  - Middleware Next.js → redirect ke login jika tidak ada token

- [ ] Layout dashboard
  - `app/(dashboard)/layout.tsx`
  - `components/layout/AppSidebar.tsx` → nav items dari `nav.config.ts`
  - `components/layout/AppHeader.tsx` → nama user + logout button

- [ ] Reusable components
  - `components/data-table/DataTable.tsx` → Tanstack Table
  - `components/data-table/DataTableToolbar.tsx` → search + filter
  - `components/data-table/DataTablePagination.tsx`
  - `components/data-table/DataTableRowActions.tsx` → edit/delete dropdown
  - `components/forms/FormField.tsx` → labeled input wrapper
  - `hooks/use-resource.ts` → generic CRUD hook

- [ ] User management pages
  - `app/(dashboard)/users/columns.tsx`
  - `app/(dashboard)/users/page.tsx` → list + status badge
  - `app/(dashboard)/users/[id]/edit/page.tsx` → form edit + ubah status

- [ ] Feature flag page
  - `app/(dashboard)/config/page.tsx` → toggle list + edit value string/number

- [ ] Dashboard home
  - `app/(dashboard)/page.tsx` → stats cards (total users, active users)

### Acceptance Criteria ✅

```
□ Buka http://localhost:3000 → redirect ke /login
□ Login dengan superadmin@company.com → masuk dashboard
□ Menu Users → tampil tabel user dengan pagination
□ Bisa ubah status user menjadi SUSPENDED → user tidak bisa login lagi
□ Menu Config → tampil toggle list
□ Toggle maintenance_mode → nilai berubah di DB
□ Logout → kembali ke halaman login
```

---

## Phase 5 — Mobile App (KMM + CMP)

**Estimasi:** 4-5 hari  
**Dependency:** Phase 1, 3 selesai  
**Tujuan:** Mobile app launch, fetch config, handle maintenance/force-update, bisa login.

### Tasks

- [ ] Setup KMM module (`shared-kmm/`)
  - `build.gradle.kts` dengan targets: android, ios
  - Dependencies: Ktor client, kotlinx.serialization, kotlinx.coroutines, Koin

- [ ] Setup Compose Multiplatform (`composeApp/`)
  - Target: Android + iOS
  - Dependencies: Compose Navigation, Koin Compose

- [ ] Networking layer (shared-kmm)
  - `ApiClient.kt` → Ktor HttpClient, JSON serialization, auth interceptor
  - `ApiService.kt` → interface endpoints: `fetchConfig()`, `login()`, `getMe()`
  - `TokenStorage.kt` → simpan JWT (DataStore Android / NSUserDefaults iOS)

- [ ] Domain models (shared-kmm)
  - `AppConfig.kt`
  - `User.kt`
  - `AuthResponse.kt`

- [ ] Repositories (shared-kmm)
  - `ConfigRepository.kt` → `fetchConfig()`
  - `AuthRepository.kt` → `login()`, `logout()`, `getMe()`

- [ ] ViewModels (shared-kmm)
  - `AppViewModel.kt` → `loadConfig()`, states: loading/maintenance/forceUpdate/ready
  - `AuthViewModel.kt` → `login()`, `logout()`, states: idle/loading/success/error

- [ ] Compose UI (composeApp)
  - `theme/Color.kt` + `theme/AppTheme.kt`
  - `navigation/AppNavigation.kt` → routes: splash, login, home
  - `screens/SplashScreen.kt` → loading, fetch config
  - `screens/MaintenanceScreen.kt` → tampil saat maintenance_mode=true
  - `screens/ForceUpdateScreen.kt` → tampil saat force_update=true
  - `screens/auth/LoginScreen.kt` → form email + password
  - `screens/home/HomeScreen.kt` → placeholder dashboard (bisa diisi per produk)

- [ ] App startup flow
  ```
  Launch → SplashScreen → fetch /api/config
    ├─ maintenance_mode=true  → MaintenanceScreen
    ├─ force_update=true      → ForceUpdateScreen
    └─ normal                 → LoginScreen (jika belum login)
                                   └─ HomeScreen (jika sudah login)
  ```

- [ ] Android entry point (`androidApp/`)
  - `MainActivity.kt` → `setContent { AppTheme { App() } }`
  - `AndroidManifest.xml`

- [ ] iOS entry point (`iosApp/`)
  - `ContentView.swift`
  - Xcode project link ke KMM framework

### Acceptance Criteria ✅

```
□ Android app build clean (./gradlew :androidApp:assembleDebug)
□ iOS app build clean (Xcode → run di simulator)
□ Launch → SplashScreen → fetch config
□ Jika maintenance_mode=ON di admin → mobile tampilkan MaintenanceScreen
□ Login dengan valid credential → masuk HomeScreen
□ Token tersimpan, restart app → langsung HomeScreen (tidak perlu login ulang)
□ Logout → kembali ke LoginScreen
```

---

## Phase 6 — Integration & Template Finalization

**Estimasi:** 1-2 hari  
**Dependency:** Phase 1-5 selesai  
**Tujuan:** Semua layer terintegrasi, template siap di-clone.

### Tasks

- [ ] End-to-end integration test manual
  - Jalankan semua service bersamaan: docker-compose + backend + admin + mobile
  - Test flow lengkap: admin toggle maintenance → mobile tampilkan maintenance screen

- [ ] Polish error handling
  - Backend: response format konsisten untuk semua error
  - Admin: toast notification untuk sukses/gagal operasi
  - Mobile: error screen dengan tombol retry

- [ ] Template cleanup
  - Hapus kode placeholder yang tidak diperlukan
  - Pastikan semua `TODO` dan contoh sudah diberi label jelas
  - Verifikasi `application-local.yml.example` sudah lengkap

- [ ] Update docs sesuai implementasi aktual
  - Update `GETTING_STARTED.md` dengan perintah yang sudah diverifikasi
  - Update `NEW_PRODUCT_GUIDE.md` dengan checklist yang akurat

- [ ] Buat seed script
  - Script untuk reset database ke state awal (untuk demo / setup pertama)

### Acceptance Criteria ✅ — Template Ready

```
□ git clone → docker-compose up → ./gradlew bootRun → npm run dev
  → semua jalan tanpa error

□ Admin panel berfungsi penuh:
  - Login, user management, feature flag toggle

□ Mobile app berfungsi penuh:
  - Launch, fetch config, login, logout

□ Maintenance mode flow bekerja end-to-end:
  - Toggle di admin → mobile langsung tampilkan halaman maintenance

□ Developer baru bisa setup dari nol mengikuti GETTING_STARTED.md
  tanpa bantuan tambahan

□ Template siap di-clone untuk produk baru ✅
```

---

## Summary Timeline

```
Minggu 1:
  Hari 1-3  │ Phase 1: Backend Foundation
  Hari 4-5  │ Phase 2: RBAC + User Management

Minggu 2:
  Hari 1    │ Phase 3: Feature Flag (paralel dengan admin)
  Hari 2-5  │ Phase 4: Admin Panel

Minggu 3:
  Hari 1-5  │ Phase 5: Mobile App (KMM + CMP)

Minggu 4:
  Hari 1-2  │ Phase 6: Integration & Finalize

Total: ~3-4 minggu (solo developer)
       ~2 minggu (2 developer: 1 backend/admin, 1 mobile)
```

---

## Setelah Template Selesai

Template ini menjadi **titik awal baru** untuk setiap produk:

```
Template selesai ✅
      │
      ├─► Produk A: Clone → ubah branding → tambah modul domain A
      ├─► Produk B: Clone → ubah branding → tambah modul domain B
      └─► Produk C: Clone → ubah branding → tambah modul domain C

Setiap produk sudah punya:
  ✓ Auth + JWT
  ✓ User management (END_USER + STAFF + SUPER_ADMIN)
  ✓ RBAC (permission per modul)
  ✓ Feature flag (toggle dari admin)
  ✓ Admin panel siap pakai
  ✓ Mobile app dengan startup flow yang benar
```
