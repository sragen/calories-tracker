# Calories Tracker — Product Design Overview

App kalori tracker berbasis AI untuk membantu user diet dan mengatur pola makan sehat.

---

## Visi Produk

**"Track makanan semudah foto — tanpa ribet input manual."**

User foto makanan → AI identifikasi + hitung kalori/makro → masuk diary otomatis.

---

## Stack (dari Template)

| Layer | Teknologi |
|---|---|
| Backend | Spring Boot 3.4.1 (Kotlin 2.1, Java 21) + PostgreSQL |
| Admin Panel | Next.js 15 + Shadcn/UI + TanStack Table |
| Mobile | Compose Multiplatform + KMM (Android + iOS) |
| Auth | JWT (access 24h / refresh 30d) |
| File Storage | MinIO (local) / S3 (prod) |
| Infra | Docker Compose |

---

## Domain Modules (Tambahan)

```
backend/modules/
├── food/           # Master database makanan (TKPI + Open Food Facts)
├── meal-log/       # Daily food diary
├── ai-scan/        # AI foto recognition (OpenAI/Gemini Vision)
├── body-profile/   # BMR/TDEE calculator + stats tubuh user
├── daily-goal/     # Target kalori & makro harian
└── subscription/   # Free vs Premium + payment gateway
```

---

## Model Bisnis

### Subscription Tiers

| Fitur | Free | Premium |
|---|---|---|
| Log manual (cari nama) | 10x / hari | Unlimited |
| Barcode scan | ✅ | ✅ |
| **AI foto scan** | ❌ | ✅ |
| BMR / TDEE calculator | ✅ | ✅ |
| Target kalori harian | ✅ | ✅ |
| Macro tracking (hari ini) | ✅ | ✅ |
| Riwayat 90 hari | ❌ | ✅ |
| Export data CSV | ❌ | ✅ |

### Harga

| Plan | Harga | Durasi |
|---|---|---|
| Free | Rp 0 | Selamanya |
| Premium Monthly | Rp 49.000 | 30 hari |
| Premium Yearly | Rp 399.000 | 365 hari (hemat 32%) |

---

## Integrasi Eksternal

| Kebutuhan | Service |
|---|---|
| AI food recognition | OpenAI Vision API (`gpt-4o`) atau Google Gemini Vision |
| Barcode lookup | Open Food Facts API (free, no key required) |
| Food photo storage | MinIO / S3 (sudah di template) |
| Payment gateway | Midtrans (QRIS, Virtual Account, Kartu Kredit) |

---

## Admin Panel Scope

| Halaman | Fungsi |
|---|---|
| Dashboard | DAU/MAU, total log hari ini, revenue summary |
| Food Database | CRUD makanan, verifikasi submisi user, import TKPI |
| Users | Daftar user, subscription status, suspend/aktifkan |
| Subscriptions | Riwayat transaksi, refund manual |
| Analytics | Top foods, usage trends, revenue chart |
| Config / Feature Flags | Toggle AI scan, atur free tier limits |

---

## Dokumen Terkait

- [Database Schema](DATABASE.md) — Semua tabel SQL
- [API Specification](API_SPEC.md) — Semua endpoint REST
- [Mobile Flow](MOBILE_FLOW.md) — Screen list + navigasi
- [Admin Panel Design](ADMIN_PANEL.md) — Halaman admin + komponen
- [Integrations](INTEGRATIONS.md) — AI scan, barcode, payment
- [Implementation Roadmap](ROADMAP.md) — Phase-by-phase plan
