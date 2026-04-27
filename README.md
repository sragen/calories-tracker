# CalSnap

> Snap your food. Track your macros. Hit your goals.

CalSnap is an AI-powered nutrition tracking app for people serious about their fitness and health. Point your camera at any meal, get instant calorie and macro breakdowns, and stay on top of your daily targets — all in one place, across Android and iOS.

## What CalSnap Does

Most calorie trackers make logging food feel like a chore. CalSnap removes the friction:

- **AI Food Scan** — Take a photo of your meal and Gemini AI identifies the food and estimates calories, protein, carbs, and fat automatically.
- **Barcode Scanner** — Scan packaged food barcodes to pull nutrition data from the OpenFoodFacts database.
- **Meal Logging** — Log breakfast, lunch, dinner, and snacks. Search from a curated food database or submit your own.
- **Daily Goals** — Set personalized calorie and macro targets calculated from your body profile (BMR + TDEE).
- **Body Profile** — Track your weight, height, age, and activity level. CalSnap recalculates your goals as your body changes.
- **Analytics** — Weekly and monthly charts for calories, macros, and weight trends to see your progress over time.
- **Premium Subscriptions** — Unlock AI scan and advanced analytics through in-app subscription via Midtrans.

## Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.4.1 (Kotlin 2.1, Java 21) + PostgreSQL + Flyway |
| Admin Panel | Next.js 16 + Shadcn/UI + Tanstack Table v8 |
| Mobile | KMM + Compose Multiplatform 1.7.3 (Android & iOS) |
| Auth | Spring Security + JWT (access 24h / refresh 30d) |
| AI | Google Gemini (food scan) |
| Food Database | OpenFoodFacts API (barcode lookup) |
| Storage | MinIO (food images) |
| Payments | Midtrans (subscription billing) |
| Local Dev | Docker Compose (PostgreSQL port 5433, MinIO) |

## Repository Structure

```
calsnap/
├── backend/          # Spring Boot REST API
├── admin/            # Admin panel (Next.js)
├── mobile/
│   ├── shared/       # KMM shared logic (network, repositories, storage)
│   ├── composeApp/   # Compose Multiplatform shared UI
│   ├── androidApp/   # Android entry point
│   └── iosApp/       # iOS entry point (Swift)
├── docs/             # Technical and product documentation
└── docker-compose.yml
```

## Quick Start

```bash
# 1. Clone and configure
git clone <repo-url> calsnap
cd calsnap
cp backend/src/main/resources/application-local.yml.example \
   backend/src/main/resources/application-local.yml
# Fill in your Gemini API key, Midtrans keys, and MinIO credentials

# 2. Start infrastructure (PostgreSQL + MinIO)
docker-compose up -d

# 3. Start backend (runs DB migrations automatically)
cd backend && ./gradlew bootRun

# 4. Start admin panel
cd admin && npm install && npm run dev

# 5. Open
# API:          http://localhost:8080
# Swagger UI:   http://localhost:8080/swagger-ui.html
# Admin Panel:  http://localhost:3000
#
# Default superadmin: superadmin@company.com / admin123
```

## Features

| Feature | Description |
|---|---|
| **AI Food Scan** | Gemini Vision identifies food from photo and returns nutrition data |
| **Barcode Lookup** | OpenFoodFacts integration for packaged food scanning |
| **Meal Logs** | Full CRUD meal logging with breakfast/lunch/dinner/snack categories |
| **Daily Goals** | Per-user calorie and macro targets with BMR/TDEE calculation |
| **Body Profile** | Weight, height, age, activity level tracking with goal recalculation |
| **Analytics** | Calorie and macro trend charts (weekly/monthly) |
| **Food Database** | Admin-managed food catalog with pending approval workflow |
| **Subscriptions** | Premium tier with Midtrans billing and feature gating |
| **User Management** | Self-register flow with RBAC (SUPER_ADMIN / ADMIN / STAFF / USER) |
| **Feature Flags** | Toggle features on/off from admin panel, synced to mobile on launch |

## Documentation

### Product
- [Product Overview](docs/product/OVERVIEW.md) — Vision, stack, features, business model
- [Database Schema](docs/product/DATABASE.md) — All SQL tables V4–V13 + ERD
- [API Specification](docs/product/API_SPEC.md) — All REST endpoints + request/response
- [Mobile Flow](docs/product/MOBILE_FLOW.md) — Screen list, navigation, wireframes
- [Admin Panel Design](docs/product/ADMIN_PANEL.md) — Admin pages and components
- [Integrations](docs/product/INTEGRATIONS.md) — AI scan, barcode, MinIO, Midtrans
- [Implementation Roadmap](docs/product/ROADMAP.md) — Phase 1–4 with checklists

### Technical
- [Getting Started](docs/GETTING_STARTED.md) — Full setup from zero
- [Architecture](docs/ARCHITECTURE.md) — Design decisions and system overview
- [Backend Guide](docs/BACKEND.md) — Spring Boot patterns, RBAC, API endpoints
- [Admin Panel Guide](docs/ADMIN_PANEL.md) — How to add new CRUD pages
- [Mobile Guide](docs/MOBILE.md) — KMM + CMP patterns, how to add screens
- [Database Schema](docs/DATABASE_SCHEMA.md) — Core template tables + ERD
