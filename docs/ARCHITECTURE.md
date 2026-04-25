# Arsitektur Sistem

## Gambaran Umum

```
┌─────────────────────────────────────────────────────────────────────┐
│                    APP TEMPLATE — MONOREPO                          │
│                                                                     │
│  ┌──────────────────┐  ┌─────────────────┐  ┌──────────────────┐  │
│  │    backend/       │  │     admin/      │  │     mobile/      │  │
│  │                   │  │                 │  │                  │  │
│  │  Spring Boot 3    │  │  Next.js 15 +   │  │  Compose MP +    │  │
│  │  Kotlin           │  │  Shadcn/UI +    │  │  KMM             │  │
│  │  PostgreSQL        │  │  Tanstack Table │  │  Android + iOS   │  │
│  │  REST API          │  │  (Custom CRUD)  │  │                  │  │
│  └────────┬──────────┘  └───────┬─────────┘  └────────┬─────────┘  │
│           │                     │                      │            │
│           └─────────────────────┴──────────────────────┘            │
│                           REST API (OpenAPI/Swagger)                │
└─────────────────────────────────────────────────────────────────────┘
```

## Prinsip Desain

**1. Clone-per-Product, bukan Multi-Tenant**
Setiap produk adalah repo mandiri hasil clone template ini. Tidak ada shared runtime antar produk — setiap produk punya database, backend, dan frontend sendiri. Isolasi penuh.

**2. Configuration over Code**
Branding, nama app, warna — semua dari file config, bukan hardcode di component. Ganti config = ganti product identity.

**3. Pattern-based Extensibility**
Menambah fitur baru = ikuti pattern yang sudah ada. Bukan framework magic, tapi konvensi yang jelas dan konsisten.

**4. Offline-capable Mobile**
Shared KMM layer menggunakan Repository pattern dengan local cache. Mobile app tetap fungsional saat network terbatas.

## Keputusan Arsitektur

### Mengapa Spring Boot + Kotlin (bukan Go atau Node.js)?
- Kotlin digunakan juga di KMM → tim mobile dan backend bisa share pengetahuan
- Spring Boot mature untuk enterprise use cases (auth, RBAC, audit)
- Kotlin coroutines native di Spring WebFlux jika diperlukan async

### Mengapa Compose Multiplatform (bukan React Native)?
- Single UI codebase untuk Android + iOS dengan Kotlin
- Shared ViewModel via StateFlow — tidak perlu bridge JS-Native
- Lebih mudah di-maintain oleh tim Kotlin daripada tim JS yang juga handle mobile

### Mengapa Custom Admin (bukan Refine/React Admin)?
- Zero framework lock-in
- Kode 100% dapat dimengerti tanpa belajar framework tambahan
- Pattern CRUD cukup sederhana untuk dibangun sendiri dengan Shadcn + Tanstack

## Data Flow

```
Mobile App                 Admin Panel
    │                           │
    │ REST API                  │ REST API
    ▼                           ▼
┌───────────────────────────────────────┐
│           Spring Boot API             │
│  ┌──────────┐  ┌───────────────────┐  │
│  │  Auth    │  │  Domain Modules   │  │
│  │  (JWT)   │  │  (User, Product,  │  │
│  └──────────┘  │   Order, dst.)    │  │
│                └──────────┬────────┘  │
└───────────────────────────┼───────────┘
                            │
                    ┌───────▼────────┐
                    │   PostgreSQL   │
                    └────────────────┘
                            │
                    ┌───────▼────────┐
                    │  MinIO/S3      │
                    │  (File Storage)│
                    └────────────────┘
```

## Environment

| Environment | Database | Storage | Notes |
|---|---|---|---|
| Local | PostgreSQL (Docker) | MinIO (Docker) | `docker-compose up` |
| Staging | PostgreSQL (managed) | S3 / R2 | Deploy via CI |
| Production | PostgreSQL (managed) | S3 / R2 | Deploy via CI |

## Roadmap Template

```
Fase 1 (Sekarang): Monorepo template
  → Semua dalam satu repo, clone per produk
  → Accept template drift antar produk

Fase 2 (Setelah 2-3 produk): Extract shared libs
  → com.company:spring-commons (Maven private)
  → @company/ui-kit (npm private)
  → com.company:kmm-core (KMP library)

Fase 3 (Tim besar): Internal platform
  → Renovate bot untuk auto-bump versi
  → CI/CD template yang bisa di-reuse
```
