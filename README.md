# App Template — InnovaRebornDiesel

Monorepo starter template untuk membangun produk baru dengan cepat.
Clone repo ini → ganti branding → mulai coding domain bisnis.

## Stack

| Layer | Teknologi |
|---|---|
| Backend | Spring Boot 3.x (Kotlin) + PostgreSQL |
| Admin Panel | Next.js 15 + Shadcn/UI + Tanstack Table |
| Mobile | Kotlin Multiplatform + Compose Multiplatform |
| Auth | Spring Security + JWT |
| Local Dev | Docker Compose |

## Struktur Repo

```
app-template/
├── backend/          # Spring Boot REST API
├── admin/            # Admin panel (Next.js)
├── mobile/
│   ├── shared-kmm/   # KMM shared logic (domain, data, ViewModel)
│   ├── composeApp/   # Compose Multiplatform shared UI
│   ├── androidApp/   # Android entry point
│   └── iosApp/       # iOS entry point
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

### Panduan Utama
- [Implementation Plan](docs/IMPLEMENTATION_PLAN.md) — Roadmap phase by phase membangun template
- [Getting Started](docs/GETTING_STARTED.md) — Setup dari nol sampai semua jalan
- [Panduan Produk Baru](docs/NEW_PRODUCT_GUIDE.md) — Checklist clone → produk baru

### Arsitektur & Fitur
- [Arsitektur Sistem](docs/ARCHITECTURE.md) — Design decisions dan system overview
- [Core Features](docs/CORE_FEATURES.md) — User management, RBAC, Feature Flag
- [Database Schema](docs/DATABASE_SCHEMA.md) — Semua tabel SQL + ERD

### Per Layer
- [Backend Guide](docs/BACKEND.md) — Spring Boot patterns, RBAC usage, API endpoints
- [Admin Panel Guide](docs/ADMIN_PANEL.md) — Cara tambah CRUD page baru
- [Mobile Guide](docs/MOBILE.md) — KMM + CMP patterns, cara tambah screen
