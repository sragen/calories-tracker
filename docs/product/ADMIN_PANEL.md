# Admin Panel Design — Calories Tracker

Stack: Next.js 15 + Shadcn/UI + TanStack Table

---

## Navigasi Admin

```
admin/src/app/(dashboard)/
├── page.tsx                        # Dashboard overview
├── foods/
│   ├── page.tsx                    # List food items
│   ├── create/page.tsx             # Tambah makanan
│   ├── [id]/edit/page.tsx          # Edit makanan
│   └── pending/page.tsx            # Review submisi user
├── users/
│   ├── page.tsx                    # List users (dari template)
│   └── [id]/subscription/page.tsx  # Detail subscription user
├── subscriptions/
│   └── page.tsx                    # Semua transaksi subscription
├── analytics/
│   └── page.tsx                    # Analytics dashboard
└── configs/
    └── page.tsx                    # Feature flags (dari template)
```

---

## Halaman 1: Dashboard Overview

**Path:** `/`

```
┌─────────────────────────────────────────────────────┐
│  Calories Tracker Admin        15 Januari 2024       │
│                                                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────┐│
│  │ DAU      │  │ MAU      │  │ Total Log│  │ Rev  ││
│  │  1,234   │  │  8,901   │  │  45,678  │  │ 4.9M ││
│  │ +12% ↑  │  │ +8% ↑   │  │ hari ini │  │ bulan││
│  └──────────┘  └──────────┘  └──────────┘  └──────┘│
│                                                     │
│  Kalori Log 7 Hari Terakhir          Top Makanan    │
│  ┌────────────────────────┐  ┌──────────────────────┐│
│  │ Chart: bar chart       │  │ 1. Nasi Putih 12,340 ││
│  │ per hari               │  │ 2. Nasi Goreng 8,901 ││
│  │                        │  │ 3. Ayam Goreng 7,234 ││
│  │                        │  │ 4. Indomie     6,120 ││
│  └────────────────────────┘  │ 5. Tempe Goreng 5,890││
│                               └──────────────────────┘│
│                                                     │
│  User Baru (7 hari)          Subscription Aktif     │
│  ┌────────────────────────┐  ┌──────────────────────┐│
│  │ Total: 234 user baru   │  │ Free:    7,456 (84%) ││
│  │ Chart: line chart      │  │ Premium: 1,445 (16%) ││
│  └────────────────────────┘  └──────────────────────┘│
└─────────────────────────────────────────────────────┘
```

**Komponen:**
- `StatsCard` — angka + persentase perubahan + ikon
- `BarChart` — daily log volume (recharts)
- `TopFoodsList` — ranking makanan
- `PieChart` — distribusi subscription tier

---

## Halaman 2: Food Database

**Path:** `/foods`

```
┌─────────────────────────────────────────────────────┐
│  Food Database                                      │
│                                                     │
│  [ 🔍 Cari nama makanan... ]  [Sumber ▼] [Kategori ▼] │
│                                                     │
│  [+ Tambah Makanan]  [Import TKPI CSV]  [3 Pending Review] │
│                                                     │
│  ┌───┬──────────────────┬──────┬─────┬──────┬──────┐│
│  │   │ Nama             │ Kal  │ Src │ Vrfy │ Aksi ││
│  ├───┼──────────────────┼──────┼─────┼──────┼──────┤│
│  │ ☐ │ Nasi Putih       │ 175  │TKPI │ ✅   │ ✎ 🗑 ││
│  │ ☐ │ Nasi Goreng      │ 182  │TKPI │ ✅   │ ✎ 🗑 ││
│  │ ☐ │ Pecel Madiun     │ 120  │USER │ ⏳   │ ✎ ✓  ││
│  │ ☐ │ Indomie Goreng   │ 422  │ADMN │ ✅   │ ✎ 🗑 ││
│  └───┴──────────────────┴──────┴─────┴──────┴──────┘│
│                                                     │
│  Showing 1-20 of 523   [<] [1] [2] [3] ... [27] [>]│
└─────────────────────────────────────────────────────┘
```

**Filter:**
- Sumber: Semua | TKPI | Open Food Facts | Admin | User Submission
- Kategori: dropdown semua kategori
- Status: Semua | Verified | Pending Review

**Bulk actions:** Hapus terpilih, Verifikasi terpilih

---

## Halaman 3: Tambah / Edit Makanan

**Path:** `/foods/create` dan `/foods/[id]/edit`

```
┌─────────────────────────────────────────────────────┐
│  ← Tambah Makanan                                   │
│                                                     │
│  Nama Makanan *                                     │
│  [ Nasi Goreng Spesial                           ]  │
│                                                     │
│  Nama (Inggris)                                     │
│  [ Special Fried Rice                            ]  │
│                                                     │
│  Kategori *                                         │
│  [ Nasi & Sereal                              ▼ ]   │
│                                                     │
│  ─── Nutrisi per 100g ────────────────────────────  │
│  Kalori (kcal) *   Protein (g) *                    │
│  [ 182          ]  [ 4.7       ]                    │
│                                                     │
│  Karbohidrat (g) * Lemak (g) *                      │
│  [ 37.2         ]  [ 2.1       ]                    │
│                                                     │
│  Serat (g)         Gula (g)                         │
│  [ 0.8          ]  [ 0.5       ]                    │
│                                                     │
│  Sodium (mg)                                        │
│  [ 450          ]                                   │
│                                                     │
│  ─── Porsi Default ───────────────────────────────  │
│  Berat default (g) *   Deskripsi porsi              │
│  [ 250             ]   [ 1 porsi (250g)          ]  │
│                                                     │
│  Barcode (jika ada)                                 │
│  [ 8992388123456                               ]    │
│                                                     │
│  Status                                             │
│  ● Verified  ○ Pending Review                       │
│                                                     │
│  [ Batal ]                    [ Simpan Makanan ]    │
└─────────────────────────────────────────────────────┘
```

---

## Halaman 4: Pending Review

**Path:** `/foods/pending`

Daftar submisi makanan dari user yang menunggu verifikasi.

```
┌─────────────────────────────────────────────────────┐
│  Pending Review (3 item)                            │
│                                                     │
│  ┌──────────────────────────────────────────────┐   │
│  │ Pecel Madiun                  Dikirim 2j lalu│   │
│  │ Oleh: budi@email.com                         │   │
│  │ Kategori: Masakan Indonesia                  │   │
│  │ Kalori: 120 kcal/100g                        │   │
│  │ Protein: 5.2g | Karbo: 18.3g | Lemak: 3.1g  │   │
│  │                                              │   │
│  │ [✎ Edit dulu]  [✓ Verifikasi]  [✕ Tolak]   │   │
│  └──────────────────────────────────────────────┘   │
│                                                     │
│  ┌──────────────────────────────────────────────┐   │
│  │ Es Teh Manis Warung              Dikirim 5j lalu│  │
│  │ ...                                          │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

---

## Halaman 5: Users

**Path:** `/users`

Extend dari template users page. Tambahan kolom: Subscription, Total Log.

```
┌──┬─────────────────┬────────┬─────────────┬──────────┬──────┐
│  │ Nama / Email    │ Status │ Subscription│ Total Log│ Aksi │
├──┼─────────────────┼────────┼─────────────┼──────────┼──────┤
│☐ │ Budi Santoso    │ ACTIVE │ 🌟 Premium  │   456    │ 👁 ✎ │
│  │ budi@email.com  │        │ s/d 14 Feb  │          │      │
├──┼─────────────────┼────────┼─────────────┼──────────┼──────┤
│☐ │ Siti Rahayu     │ ACTIVE │ Free        │   23     │ 👁 ✎ │
│  │ siti@email.com  │        │             │          │      │
└──┴─────────────────┴────────┴─────────────┴──────────┴──────┘
```

**Action per user:** View detail, Edit status, Grant/Revoke premium manual

---

## Halaman 6: Detail Subscription User

**Path:** `/users/[id]/subscription`

```
┌─────────────────────────────────────────────────────┐
│  ← Subscription — Budi Santoso                      │
│                                                     │
│  Status Saat Ini: 🌟 Premium Bulanan                │
│  Aktif sejak: 15 Jan 2024                           │
│  Kadaluarsa: 14 Feb 2024                            │
│                                                     │
│  [  Grant Premium Manual  ]  [  Cabut Premium  ]    │
│                                                     │
│  Riwayat Transaksi:                                 │
│  ┌──────────────────────────────────────────────┐   │
│  │ 15 Jan 2024  Premium Bulanan  Rp 49.000  ✅  │   │
│  │ 15 Des 2023  Premium Bulanan  Rp 49.000  ✅  │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

---

## Halaman 7: Analytics

**Path:** `/analytics`

```
┌─────────────────────────────────────────────────────┐
│  Analytics              [7 Hari ▼] [30 Hari] [90 Hr]│
│                                                     │
│  ─── Revenue ───────────────────────────────────    │
│  ┌────────────────────────────────────────────────┐ │
│  │ Chart: Revenue per hari (bar chart)            │ │
│  │ Total bulan ini: Rp 4.900.000                  │ │
│  │ ARPU: Rp 3.391/user                            │ │
│  └────────────────────────────────────────────────┘ │
│                                                     │
│  ─── User Retention ────────────────────────────    │
│  ┌────────────────────────────────────────────────┐ │
│  │ Cohort table: Week 1-4 retention               │ │
│  │ Jan 15: 100% → 72% → 58% → 45%               │ │
│  └────────────────────────────────────────────────┘ │
│                                                     │
│  ─── Top Foods ─────────────────────────────────    │
│  ┌────────────────────────────────────────────────┐ │
│  │ 1. Nasi Putih         12,340 log               │ │
│  │ 2. Nasi Goreng         8,901 log               │ │
│  │ 3. Ayam Goreng         7,234 log               │ │
│  └────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

---

## Halaman 8: Feature Flags & Config

**Path:** `/configs` (sudah ada di template, extend dengan config baru)

| Config Key | Label | Type | Default |
|---|---|---|---|
| `ai_scan_enabled` | AI Scan Aktif | BOOLEAN | true |
| `free_daily_log_limit` | Limit Log Harian Free | NUMBER | 10 |
| `barcode_scan_enabled` | Barcode Scan Aktif | BOOLEAN | true |
| `payment_enabled` | Payment Gateway Aktif | BOOLEAN | false |
| `maintenance_mode` | Mode Maintenance | BOOLEAN | false |

---

## Komponen Reusable

```
admin/src/components/
├── data-table.tsx              # Dari template, reused
├── stats-card.tsx              # Kartu angka + trend
├── food-form.tsx               # Form tambah/edit makanan
├── subscription-badge.tsx      # Badge FREE / PREMIUM
└── charts/
    ├── revenue-chart.tsx       # Bar chart revenue
    ├── daily-log-chart.tsx     # Bar chart daily logs
    └── retention-table.tsx     # Cohort retention
```

---

## Nav Config

```typescript
// admin/src/config/nav.config.ts

export const navItems = [
  { href: '/',              label: 'Dashboard',    icon: 'LayoutDashboard' },
  { href: '/foods',         label: 'Food Database', icon: 'Salad',
    children: [
      { href: '/foods/pending', label: 'Pending Review', badge: 'pendingCount' }
    ]
  },
  { href: '/users',         label: 'Users',         icon: 'Users' },
  { href: '/subscriptions', label: 'Subscriptions', icon: 'CreditCard' },
  { href: '/analytics',     label: 'Analytics',     icon: 'BarChart3' },
  { href: '/configs',       label: 'Config',        icon: 'Settings' },
]
```
