# App Template — InnovaRebornDiesel

Monorepo starter template untuk membangun produk baru dengan cepat.
Clone repo ini → ganti branding → mulai coding domain bisnis.

## Stack

| Layer | Teknologi |
|---|---|
| Backend | Spring Boot 3.4.1 (Kotlin 2.1, Java 21) + PostgreSQL + Flyway |
| Admin Panel | Next.js 16 + Shadcn/UI + Tanstack Table v8 |
| Mobile | KMM + Compose Multiplatform 1.7.3 (Android/iOS) |
| Auth | Spring Security + JWT (access 24h / refresh 30d) |
| Local Dev | Docker Compose (PostgreSQL port 5433, MinIO) |

## Struktur Repo

```
app-template/
├── backend/          # Spring Boot REST API
├── admin/            # Admin panel (Next.js)
├── mobile/
│   ├── shared/       # KMM shared logic (network, data, storage)
│   ├── composeApp/   # Compose Multiplatform shared UI
│   ├── androidApp/   # Android entry point
│   └── iosApp/       # iOS entry point (Swift)
├── docs/             # Dokumentasi teknis
└── docker-compose.yml
```

## Quick Start

```bash
# 1. Clone dan setup
git clone <repo-url> nama-produk-baru
cd nama-produk-baru
cp backend/src/main/resources/application-local.yml.example \
   backend/src/main/resources/application-local.yml

# 2. Jalankan infrastruktur (PostgreSQL + MinIO)
docker-compose up -d

# 3. Jalankan backend
cd backend && ./gradlew bootRun

# 4. Jalankan admin panel
cd admin && npm install && npm run dev

# 5. Buka
# API:          http://localhost:8080
# Swagger UI:   http://localhost:8080/swagger-ui.html
# Admin Panel:  http://localhost:3000
#
# Login superadmin: superadmin@company.com / admin123
```

## Cara Buat Produk Baru

Lihat panduan lengkap: [docs/NEW_PRODUCT_GUIDE.md](docs/NEW_PRODUCT_GUIDE.md)

Ringkasan 5 langkah:
1. Clone repo ini
2. Ganti branding di `config/app.config.ts` dan `theme/Color.kt`
3. Tambah domain module di backend (`modules/`)
4. Tambah halaman CRUD di admin (`pages/`)
5. Tambah screen di mobile (`screens/`)

## Core Features

Tiga fitur yang selalu ada di setiap produk:

| Fitur | Deskripsi |
|---|---|
| **User Management** | End user self-register, Staff dibuat admin, SUPER_ADMIN via seed SQL |
| **Admin RBAC** | 4 role (SUPER_ADMIN/ADMIN/STAFF/USER), permission per modul di DB |
| **Feature Flag** | Toggle on/off dari admin panel, mobile fetch saat launch |

## Dokumentasi

### Product Design — Calories Tracker
- [Product Overview](docs/product/OVERVIEW.md) — Visi, stack, fitur, model bisnis
- [Database Schema](docs/product/DATABASE.md) — Semua tabel SQL V4–V12 + ERD
- [API Specification](docs/product/API_SPEC.md) — Semua endpoint REST + request/response
- [Mobile Flow](docs/product/MOBILE_FLOW.md) — Screen list + navigasi + wireframe
- [Admin Panel Design](docs/product/ADMIN_PANEL.md) — Halaman admin + komponen
- [Integrations](docs/product/INTEGRATIONS.md) — AI scan, barcode, MinIO, Midtrans
- [Implementation Roadmap](docs/product/ROADMAP.md) — Phase 1–4 dengan checklist

### Template Docs (Framework)
- [Implementation Plan](docs/IMPLEMENTATION_PLAN.md) — Roadmap phase by phase membangun template
- [Getting Started](docs/GETTING_STARTED.md) — Setup dari nol sampai semua jalan
- [Panduan Produk Baru](docs/NEW_PRODUCT_GUIDE.md) — Checklist clone → produk baru
- [Arsitektur Sistem](docs/ARCHITECTURE.md) — Design decisions dan system overview
- [Core Features](docs/CORE_FEATURES.md) — User management, RBAC, Feature Flag
- [Database Schema Template](docs/DATABASE_SCHEMA.md) — Tabel core template + ERD
- [Backend Guide](docs/BACKEND.md) — Spring Boot patterns, RBAC usage, API endpoints
- [Admin Panel Guide](docs/ADMIN_PANEL.md) — Cara tambah CRUD page baru
- [Mobile Guide](docs/MOBILE.md) — KMM + CMP patterns, cara tambah screen
