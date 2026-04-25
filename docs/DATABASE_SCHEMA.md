# Database Schema

Schema lengkap untuk semua tabel core template. Dijalankan otomatis via Flyway saat `bootRun`.

---

## V1 — Tabel Core (Users, Roles, Files)

```sql
-- V1__init_core.sql

-- ─────────────────────────────────────────
-- ROLES
-- ─────────────────────────────────────────
CREATE TABLE roles (
    name        VARCHAR(50)  PRIMARY KEY,
    label       VARCHAR(100) NOT NULL,
    description TEXT
);

INSERT INTO roles (name, label, description) VALUES
    ('SUPER_ADMIN', 'Super Admin', 'Akses penuh ke semua fitur dan konfigurasi'),
    ('ADMIN',       'Admin',       'Kelola users dan konten, tidak bisa hapus data'),
    ('STAFF',       'Staff',       'Akses read-only ke modul tertentu'),
    ('USER',        'User',        'Pengguna mobile app, tidak bisa masuk admin panel');

-- ─────────────────────────────────────────
-- USERS
-- ─────────────────────────────────────────
CREATE TABLE users (
    id          BIGSERIAL    PRIMARY KEY,
    email       VARCHAR(255) UNIQUE,
    phone       VARCHAR(20)  UNIQUE,
    password    VARCHAR(255),              -- bcrypt hash, NULL jika login via OTP
    name        VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'USER' REFERENCES roles(name),
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    -- Status: ACTIVE | INACTIVE | SUSPENDED
    avatar_url  VARCHAR(500),
    fcm_token   VARCHAR(500),              -- Firebase Cloud Messaging token
    last_login  TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP                  -- soft delete
);

CREATE INDEX idx_users_email  ON users(email)  WHERE deleted_at IS NULL;
CREATE INDEX idx_users_phone  ON users(phone)  WHERE deleted_at IS NULL;
CREATE INDEX idx_users_role   ON users(role)   WHERE deleted_at IS NULL;
CREATE INDEX idx_users_status ON users(status) WHERE deleted_at IS NULL;

-- Seed super admin (ganti password setelah deploy pertama!)
INSERT INTO users (email, password, name, role, status) VALUES
    ('superadmin@company.com',
     '$2a$10$placeholder_hash',   -- ganti dengan bcrypt hash password asli
     'Super Admin',
     'SUPER_ADMIN',
     'ACTIVE');

-- ─────────────────────────────────────────
-- REFRESH TOKENS
-- ─────────────────────────────────────────
CREATE TABLE refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    revoked_at  TIMESTAMP                -- NULL = masih valid
);

CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- ─────────────────────────────────────────
-- FILE UPLOADS
-- ─────────────────────────────────────────
CREATE TABLE uploaded_files (
    id           BIGSERIAL    PRIMARY KEY,
    filename     VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes   BIGINT       NOT NULL,
    url          VARCHAR(500) NOT NULL,
    uploaded_by  BIGINT       REFERENCES users(id),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

---

## V2 — Role Permissions

```sql
-- V2__role_permissions.sql

CREATE TABLE role_permissions (
    role        VARCHAR(50) NOT NULL REFERENCES roles(name),
    module      VARCHAR(50) NOT NULL,
    can_read    BOOLEAN     NOT NULL DEFAULT TRUE,
    can_write   BOOLEAN     NOT NULL DEFAULT FALSE,
    can_delete  BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (role, module)
);

-- SUPER_ADMIN: akses penuh
INSERT INTO role_permissions (role, module, can_read, can_write, can_delete) VALUES
    ('SUPER_ADMIN', 'USERS',   true, true, true),
    ('SUPER_ADMIN', 'CONFIG',  true, true, true),
    ('SUPER_ADMIN', 'FILES',   true, true, true),
    ('SUPER_ADMIN', 'REPORTS', true, true, true);

-- ADMIN: read+write, tidak bisa delete
INSERT INTO role_permissions (role, module, can_read, can_write, can_delete) VALUES
    ('ADMIN', 'USERS',   true, true,  false),
    ('ADMIN', 'CONFIG',  true, true,  false),
    ('ADMIN', 'FILES',   true, true,  false),
    ('ADMIN', 'REPORTS', true, false, false);

-- STAFF: read-only ke USERS saja
INSERT INTO role_permissions (role, module, can_read, can_write, can_delete) VALUES
    ('STAFF', 'USERS',   true,  false, false),
    ('STAFF', 'CONFIG',  false, false, false),
    ('STAFF', 'FILES',   false, false, false),
    ('STAFF', 'REPORTS', true,  false, false);

-- USER: tidak punya permission admin panel sama sekali (dikontrol di SecurityConfig)
```

**Cara tambah modul baru** (contoh: menambah modul PRODUCTS):

```sql
-- V5__add_products_permission.sql
INSERT INTO role_permissions (role, module, can_read, can_write, can_delete) VALUES
    ('SUPER_ADMIN', 'PRODUCTS', true,  true,  true),
    ('ADMIN',       'PRODUCTS', true,  true,  false),
    ('STAFF',       'PRODUCTS', true,  false, false);
```

---

## V3 — App Config (Feature Flags)

```sql
-- V3__app_configs.sql

CREATE TABLE app_configs (
    id          BIGSERIAL    PRIMARY KEY,
    key         VARCHAR(100) NOT NULL UNIQUE,
    value       TEXT         NOT NULL,
    type        VARCHAR(20)  NOT NULL DEFAULT 'BOOLEAN',
    -- Type: BOOLEAN | STRING | NUMBER | JSON
    label       VARCHAR(255),             -- label yang tampil di admin panel
    description TEXT,                     -- penjelasan untuk admin
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_by  BIGINT       REFERENCES users(id)
);

-- Config default
INSERT INTO app_configs (key, value, type, label, description) VALUES
    ('maintenance_mode',
     'false', 'BOOLEAN',
     'Mode Maintenance',
     'Tampilkan halaman maintenance di mobile app dan blokir semua aksi'),

    ('force_update',
     'false', 'BOOLEAN',
     'Paksa Update App',
     'Paksa user update app ke versi minimum sebelum bisa digunakan'),

    ('min_app_version',
     '1.0.0', 'STRING',
     'Versi Minimum App',
     'Versi minimum app yang diizinkan, digunakan bersama force_update'),

    ('push_notification',
     'true', 'BOOLEAN',
     'Push Notification',
     'Toggle pengiriman push notification ke semua user'),

    ('promo_banner_url',
     '', 'STRING',
     'URL Banner Promo',
     'URL gambar banner promo di home screen mobile. Kosongkan untuk sembunyikan'),

    ('max_retry_login',
     '5', 'NUMBER',
     'Maks Percobaan Login',
     'Jumlah percobaan login yang gagal sebelum akun dikunci sementara (dalam menit: 15)');
```

---

## Template Migration: Cara Tambah Tabel Domain Baru

Setiap modul domain baru butuh migration SQL sendiri. Ikuti konvensi ini:

```sql
-- V4__create_products.sql  (ganti nomor versi urut dari migration terakhir)

CREATE TABLE products (
    -- ID dan timestamps SELALU ada (dari BaseEntity)
    id          BIGSERIAL    PRIMARY KEY,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP,                           -- soft delete

    -- Field domain spesifik
    name        VARCHAR(255) NOT NULL,
    price       NUMERIC(15,2) NOT NULL,
    description TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,

    -- Relasi ke user (jika ada ownership)
    created_by  BIGINT       REFERENCES users(id)
);

-- Index untuk query yang sering dipakai
CREATE INDEX idx_products_name ON products(name) WHERE deleted_at IS NULL;
```

**Konvensi naming:**
- Tabel: `snake_case` plural (`products`, `order_items`)
- Kolom: `snake_case` (`created_at`, `is_active`)
- Index: `idx_{table}_{column}` (`idx_products_name`)
- Migration file: `V{n}__{deskripsi_singkat}.sql` (double underscore)

---

## Entity Relationship Diagram

```
┌──────────┐       ┌──────────────────┐       ┌──────────────┐
│  roles   │──────▶│      users       │◀──────│refresh_tokens│
│          │       │                  │       │              │
│ name     │       │ id               │       │ id           │
│ label    │       │ email            │       │ user_id (FK) │
│          │       │ phone            │       │ token        │
└──────────┘       │ password         │       │ expires_at   │
                   │ name             │       └──────────────┘
┌──────────────┐   │ role (FK)        │
│role_perms    │   │ status           │       ┌──────────────┐
│              │   │ avatar_url       │◀──────│uploaded_files│
│ role (FK)    │   │ fcm_token        │       │              │
│ module       │   │ last_login       │       │ id           │
│ can_read     │   │ created_at       │       │ filename     │
│ can_write    │   │ updated_at       │       │ url          │
│ can_delete   │   │ deleted_at       │       │ uploaded_by  │
└──────────────┘   └──────────────────┘       └──────────────┘

┌─────────────────────────────────┐
│          app_configs            │
│                                 │
│ id                              │
│ key        (unique)             │
│ value                           │
│ type       (BOOLEAN|STRING|...) │
│ label                           │
│ description                     │
│ is_active                       │
│ updated_by (FK → users)         │
└─────────────────────────────────┘
```
