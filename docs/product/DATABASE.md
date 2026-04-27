# Database Schema — Calories Tracker

Schema lengkap untuk semua tabel produk. V1–V3 sudah ada di template (users, role_permissions, app_configs).
Tabel produk dimulai dari V4.

---

## Dependency dari Template

Tabel yang sudah ada dan digunakan:
- `users` — user auth, FCM token
- `roles` / `role_permissions` — RBAC admin panel
- `app_configs` — feature flags (ai_scan_enabled, free_tier_daily_limit)
- `uploaded_files` — referensi foto scan makanan

---

## V4 — Food Categories

```sql
-- V4__food_categories.sql

CREATE TABLE food_categories (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    name_en    VARCHAR(100),
    icon       VARCHAR(10),
    sort_order INTEGER      NOT NULL DEFAULT 0,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO food_categories (name, name_en, icon, sort_order) VALUES
    ('Nasi & Sereal',       'Rice & Grains',       '🍚', 1),
    ('Lauk Pauk',           'Side Dishes',         '🍖', 2),
    ('Sayuran',             'Vegetables',          '🥦', 3),
    ('Buah-buahan',         'Fruits',              '🍎', 4),
    ('Minuman',             'Beverages',           '🥤', 5),
    ('Snack & Camilan',     'Snacks',              '🍪', 6),
    ('Makanan Cepat Saji',  'Fast Food',           '🍔', 7),
    ('Produk Kemasan',      'Packaged Products',   '📦', 8),
    ('Olahan Susu',         'Dairy',               '🥛', 9),
    ('Masakan Indonesia',   'Indonesian Cuisine',  '🍽️', 10),
    ('Lainnya',             'Others',              '❓', 99);
```

---

## V5 — Food Items

```sql
-- V5__food_items.sql

CREATE TABLE food_items (
    id                  BIGSERIAL     PRIMARY KEY,
    name                VARCHAR(255)  NOT NULL,
    name_en             VARCHAR(255),
    category_id         BIGINT        REFERENCES food_categories(id),

    -- Nutrisi per 100g
    calories_per_100g   NUMERIC(8,2)  NOT NULL,
    protein_per_100g    NUMERIC(8,2)  NOT NULL DEFAULT 0,
    carbs_per_100g      NUMERIC(8,2)  NOT NULL DEFAULT 0,
    fat_per_100g        NUMERIC(8,2)  NOT NULL DEFAULT 0,
    fiber_per_100g      NUMERIC(8,2)  DEFAULT 0,
    sugar_per_100g      NUMERIC(8,2)  DEFAULT 0,
    sodium_per_100mg    NUMERIC(8,2)  DEFAULT 0,

    -- Porsi default (untuk tampilan awal di log)
    default_serving_g   NUMERIC(8,2)  NOT NULL DEFAULT 100,
    serving_description VARCHAR(100),     -- contoh: "1 piring (200g)"

    -- Identifikasi
    barcode             VARCHAR(50)   UNIQUE,

    -- Metadata sumber
    source              VARCHAR(20)   NOT NULL DEFAULT 'ADMIN',
    -- Values: TKPI | OPENFOODFACTS | ADMIN | USER_SUBMISSION
    external_id         VARCHAR(100),     -- ID dari Open Food Facts jika ada
    is_verified         BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,

    -- Audit
    created_by          BIGINT        REFERENCES users(id),
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP
);

CREATE INDEX idx_food_items_name     ON food_items USING gin(to_tsvector('indonesian', name)) WHERE deleted_at IS NULL;
CREATE INDEX idx_food_items_name_en  ON food_items(name_en)    WHERE deleted_at IS NULL;
CREATE INDEX idx_food_items_barcode  ON food_items(barcode)    WHERE deleted_at IS NULL AND barcode IS NOT NULL;
CREATE INDEX idx_food_items_category ON food_items(category_id) WHERE deleted_at IS NULL AND is_active = TRUE;
CREATE INDEX idx_food_items_source   ON food_items(source)     WHERE deleted_at IS NULL;
```

### Contoh Seed Data TKPI (500 makanan Indonesia)

```sql
-- Masukkan via migration atau seeder script
-- Sumber: Tabel Komposisi Pangan Indonesia (TKPI) 2017

INSERT INTO food_items (name, category_id, calories_per_100g, protein_per_100g, carbs_per_100g, fat_per_100g, default_serving_g, serving_description, source, is_verified) VALUES
    ('Nasi Putih',          1, 175, 3.1, 39.8, 0.2, 200, '1 centong (200g)',        'TKPI', TRUE),
    ('Nasi Goreng',         1, 182, 4.7, 37.2, 2.1, 250, '1 porsi (250g)',          'TKPI', TRUE),
    ('Nasi Uduk',           1, 155, 3.5, 32.1, 1.8, 200, '1 centong (200g)',        'TKPI', TRUE),
    ('Ayam Goreng',         2, 260, 27.4, 0.0, 16.8, 80,  '1 potong paha (80g)',    'TKPI', TRUE),
    ('Tempe Goreng',        2, 227, 18.3, 9.4, 14.2, 50,  '2 potong (50g)',         'TKPI', TRUE),
    ('Tahu Goreng',         2, 194, 12.6, 3.8, 14.9, 60,  '1 potong besar (60g)',   'TKPI', TRUE),
    ('Rendang Sapi',        2, 193, 19.4, 5.5, 10.3, 100, '1 porsi (100g)',         'TKPI', TRUE),
    ('Gado-gado',           2, 95,  4.2, 11.3, 3.9, 200,  '1 porsi (200g)',         'TKPI', TRUE),
    ('Soto Ayam',           2, 72,  9.1, 3.8, 2.4, 300,   '1 mangkuk (300g)',       'TKPI', TRUE),
    ('Mie Goreng',          1, 208, 6.1, 35.2, 5.8, 200,  '1 porsi (200g)',         'TKPI', TRUE),
    ('Pisang Ambon',        4, 99,  1.2, 25.8, 0.2, 100,  '1 buah sedang (100g)',   'TKPI', TRUE),
    ('Apel Merah',          4, 58,  0.3, 14.9, 0.2, 150,  '1 buah sedang (150g)',   'TKPI', TRUE),
    ('Bayam Rebus',         3, 20,  2.3, 3.2, 0.3, 100,   '1 porsi (100g)',         'TKPI', TRUE),
    ('Kangkung Tumis',      3, 44,  4.0, 6.2, 0.7, 100,   '1 porsi (100g)',         'TKPI', TRUE),
    ('Telur Ayam Rebus',    2, 162, 12.4, 0.7, 11.5, 60,  '1 butir besar (60g)',    'TKPI', TRUE),
    ('Susu Sapi Murni',     9, 61,  3.2, 4.3, 3.5, 250,   '1 gelas (250ml)',        'TKPI', TRUE),
    ('Teh Manis',           5, 50,  0.0, 12.5, 0.0, 250,  '1 gelas (250ml)',        'TKPI', TRUE),
    ('Kopi Hitam',          5, 2,   0.3, 0.0, 0.0, 200,   '1 cangkir (200ml)',      'TKPI', TRUE),
    ('Indomie Goreng',      8, 422, 9.5, 68.3, 13.1, 85,  '1 bungkus (85g)',        'ADMIN', TRUE),
    ('Roti Tawar',          1, 248, 7.9, 50.6, 1.2, 30,   '1 lembar (30g)',         'TKPI', TRUE);
    -- ... +480 lainnya via import script
```

---

## V6 — Body Profiles

```sql
-- V6__body_profiles.sql

CREATE TABLE body_profiles (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    height_cm        NUMERIC(5,1) NOT NULL,
    weight_kg        NUMERIC(5,1) NOT NULL,
    birth_date       DATE         NOT NULL,
    gender           VARCHAR(10)  NOT NULL,   -- MALE | FEMALE
    activity_level   VARCHAR(25)  NOT NULL DEFAULT 'SEDENTARY',
    -- SEDENTARY | LIGHTLY_ACTIVE | MODERATELY_ACTIVE | VERY_ACTIVE | EXTRA_ACTIVE
    goal             VARCHAR(20)  NOT NULL DEFAULT 'MAINTAIN',
    -- LOSE | MAINTAIN | GAIN
    target_weight_kg NUMERIC(5,1),

    -- Kalkulasi otomatis (disimpan untuk caching)
    bmr_kcal             NUMERIC(8,2),   -- Basal Metabolic Rate
    tdee_kcal            NUMERIC(8,2),   -- Total Daily Energy Expenditure
    recommended_calories NUMERIC(8,2),   -- TDEE +/- adjustment berdasarkan goal

    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

### Formula BMR (Mifflin-St Jeor)

```
Pria:   BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age) + 5
Wanita: BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age) - 161

TDEE = BMR × activity_multiplier
  SEDENTARY:         × 1.2
  LIGHTLY_ACTIVE:    × 1.375
  MODERATELY_ACTIVE: × 1.55
  VERY_ACTIVE:       × 1.725
  EXTRA_ACTIVE:      × 1.9

Recommended Calories:
  LOSE:     TDEE - 500  (defisit ~0.5kg/minggu)
  MAINTAIN: TDEE
  GAIN:     TDEE + 300  (surplus lean bulk)
```

---

## V7 — Daily Goals

```sql
-- V7__daily_goals.sql

CREATE TABLE daily_goals (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    target_calories  NUMERIC(8,2) NOT NULL,
    target_protein_g NUMERIC(8,2) NOT NULL DEFAULT 0,
    target_carbs_g   NUMERIC(8,2) NOT NULL DEFAULT 0,
    target_fat_g     NUMERIC(8,2) NOT NULL DEFAULT 0,
    auto_calculated  BOOLEAN      NOT NULL DEFAULT TRUE,
    -- TRUE = dari body_profile, FALSE = override manual user
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

### Formula Makro Default (dari goal)

```
LOSE:     Protein 30% | Carbs 40% | Fat 30%
MAINTAIN: Protein 25% | Carbs 50% | Fat 25%
GAIN:     Protein 25% | Carbs 55% | Fat 20%

Protein (g) = (calories × protein_ratio) / 4
Carbs   (g) = (calories × carbs_ratio)   / 4
Fat     (g) = (calories × fat_ratio)     / 9
```

---

## V8 — Meal Logs

```sql
-- V8__meal_logs.sql

CREATE TABLE meal_logs (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    food_item_id        BIGINT       NOT NULL REFERENCES food_items(id),
    quantity_g          NUMERIC(8,2) NOT NULL,
    meal_type           VARCHAR(20)  NOT NULL DEFAULT 'SNACK',
    -- BREAKFAST | LUNCH | DINNER | SNACK

    -- Snapshot nilai nutrisi saat logging (immutable, meski food_items berubah nanti)
    calories_snapshot   NUMERIC(8,2) NOT NULL,
    protein_g_snapshot  NUMERIC(8,2) NOT NULL DEFAULT 0,
    carbs_g_snapshot    NUMERIC(8,2) NOT NULL DEFAULT 0,
    fat_g_snapshot      NUMERIC(8,2) NOT NULL DEFAULT 0,

    -- Metadata AI scan (jika dari foto)
    ai_scan_photo_url   VARCHAR(500),
    ai_confidence       NUMERIC(4,3),   -- 0.000 - 1.000

    logged_at           DATE         NOT NULL DEFAULT CURRENT_DATE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP
);

CREATE INDEX idx_meal_logs_user_date ON meal_logs(user_id, logged_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_meal_logs_food      ON meal_logs(food_item_id)        WHERE deleted_at IS NULL;
```

---

## V9 — Subscription Plans

```sql
-- V9__subscription_plans.sql

CREATE TABLE subscription_plans (
    id                    BIGSERIAL     PRIMARY KEY,
    name                  VARCHAR(50)   NOT NULL UNIQUE,
    label                 VARCHAR(100)  NOT NULL,
    price_idr             NUMERIC(12,2) NOT NULL DEFAULT 0,
    duration_days         INTEGER       NOT NULL DEFAULT 0,   -- 0 = selamanya
    max_logs_per_day      INTEGER       NOT NULL DEFAULT 10,  -- -1 = unlimited
    ai_scan_enabled       BOOLEAN       NOT NULL DEFAULT FALSE,
    macro_history_days    INTEGER       NOT NULL DEFAULT 1,
    export_enabled        BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active             BOOLEAN       NOT NULL DEFAULT TRUE
);

INSERT INTO subscription_plans (name, label, price_idr, duration_days, max_logs_per_day, ai_scan_enabled, macro_history_days, export_enabled) VALUES
    ('FREE',            'Gratis',         0,       0,   10, FALSE, 1,   FALSE),
    ('PREMIUM_MONTHLY', 'Premium Bulanan', 49000,  30,  -1, TRUE,  90,  TRUE),
    ('PREMIUM_YEARLY',  'Premium Tahunan', 399000, 365, -1, TRUE,  365, TRUE);
```

---

## V10 — Subscriptions

```sql
-- V10__subscriptions.sql

CREATE TABLE subscriptions (
    id              BIGSERIAL     PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_name       VARCHAR(50)   NOT NULL REFERENCES subscription_plans(name),
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE | EXPIRED | CANCELLED | PENDING_PAYMENT

    started_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP,    -- NULL jika FREE

    -- Payment info
    payment_ref     VARCHAR(255),         -- Order ID dari Midtrans
    payment_method  VARCHAR(50),          -- QRIS | VIRTUAL_ACCOUNT | CREDIT_CARD
    amount_paid_idr NUMERIC(12,2),

    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_user_status ON subscriptions(user_id, status);
CREATE INDEX idx_subscriptions_expires     ON subscriptions(expires_at) WHERE status = 'ACTIVE';
```

---

## Cara Tambah Permission untuk Modul Baru

```sql
-- V11__add_module_permissions.sql

INSERT INTO role_permissions (role, module, can_read, can_write, can_delete) VALUES
    ('SUPER_ADMIN', 'FOODS',         TRUE, TRUE,  TRUE),
    ('SUPER_ADMIN', 'MEAL_LOGS',     TRUE, TRUE,  TRUE),
    ('SUPER_ADMIN', 'SUBSCRIPTIONS', TRUE, TRUE,  FALSE),
    ('SUPER_ADMIN', 'ANALYTICS',     TRUE, FALSE, FALSE),
    ('ADMIN',       'FOODS',         TRUE, TRUE,  FALSE),
    ('ADMIN',       'MEAL_LOGS',     TRUE, FALSE, FALSE),
    ('ADMIN',       'SUBSCRIPTIONS', TRUE, FALSE, FALSE),
    ('ADMIN',       'ANALYTICS',     TRUE, FALSE, FALSE),
    ('STAFF',       'FOODS',         TRUE, FALSE, FALSE),
    ('STAFF',       'MEAL_LOGS',     TRUE, FALSE, FALSE),
    ('STAFF',       'ANALYTICS',     TRUE, FALSE, FALSE);
```

---

## Feature Flags yang Perlu Ditambah

```sql
-- V12__caltracker_app_configs.sql

INSERT INTO app_configs (key, value, type, label, description) VALUES
    ('ai_scan_enabled',       'true',  'BOOLEAN', 'AI Scan Aktif',      'Toggle fitur AI foto scan (perlu API key OpenAI/Gemini)'),
    ('free_daily_log_limit',  '10',    'NUMBER',  'Limit Log Harian Free', 'Jumlah log per hari untuk user free tier'),
    ('barcode_scan_enabled',  'true',  'BOOLEAN', 'Barcode Scan Aktif', 'Toggle fitur barcode scan Open Food Facts'),
    ('openfoodfacts_enabled', 'true',  'BOOLEAN', 'Open Food Facts API', 'Aktifkan lookup barcode ke Open Food Facts'),
    ('payment_enabled',       'false', 'BOOLEAN', 'Payment Gateway',    'Aktifkan Midtrans payment (disable saat maintenance payment)');
```

---

## Entity Relationship Diagram

```
┌─────────────────┐        ┌──────────────────────┐
│  food_categories│──┐     │      food_items       │
│                 │  └────▶│                       │
│ id, name, icon  │        │ id, name, barcode     │
└─────────────────┘        │ *_per_100g (nutrisi)  │
                           │ source, is_verified   │
                           └──────────┬────────────┘
                                      │
┌──────────────┐                      │
│    users     │──────────────────────┼──────────────────────┐
│              │                      │                      │
│ id, email    │──┐   ┌───────────────▼──────────┐           │
│ role, status │  │   │       meal_logs           │           │
└──────────────┘  │   │                          │           │
                  │   │ id, user_id, food_item_id │           │
                  │   │ quantity_g, meal_type     │           │
                  │   │ *_snapshot (nutrisi)      │           │
                  │   │ ai_scan_photo_url         │           │
                  │   │ logged_at                 │           │
                  │   └──────────────────────────┘           │
                  │                                           │
                  │   ┌──────────────────────────┐           │
                  ├──▶│      body_profiles        │           │
                  │   │                          │           │
                  │   │ user_id, height, weight  │           │
                  │   │ gender, activity_level   │           │
                  │   │ goal, bmr, tdee          │           │
                  │   └──────────────────────────┘           │
                  │                                           │
                  │   ┌──────────────────────────┐           │
                  ├──▶│       daily_goals         │           │
                  │   │                          │           │
                  │   │ user_id, target_calories │           │
                  │   │ target_protein/carbs/fat │           │
                  │   └──────────────────────────┘           │
                  │                                           │
                  │   ┌──────────────────────────┐  ┌────────▼────────────┐
                  └──▶│      subscriptions        │  │  subscription_plans │
                      │                          │  │                     │
                      │ user_id, plan_name       │──│ name, price_idr     │
                      │ status, expires_at       │  │ ai_scan_enabled     │
                      │ payment_ref              │  │ max_logs_per_day    │
                      └──────────────────────────┘  └─────────────────────┘
```
