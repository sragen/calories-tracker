-- V1__init_core.sql
-- Core tables: roles, users, refresh_tokens, uploaded_files

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

CREATE TABLE users (
    id          BIGSERIAL    PRIMARY KEY,
    email       VARCHAR(255) UNIQUE,
    phone       VARCHAR(20)  UNIQUE,
    password    VARCHAR(255),
    name        VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'USER' REFERENCES roles(name),
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    avatar_url  VARCHAR(500),
    fcm_token   VARCHAR(500),
    last_login  TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_users_email  ON users(email)  WHERE deleted_at IS NULL;
CREATE INDEX idx_users_phone  ON users(phone)  WHERE deleted_at IS NULL;
CREATE INDEX idx_users_role   ON users(role)   WHERE deleted_at IS NULL;
CREATE INDEX idx_users_status ON users(status) WHERE deleted_at IS NULL;

-- Seed super admin — ganti password setelah deploy pertama
-- password di bawah = bcrypt("admin123")
INSERT INTO users (email, password, name, role, status) VALUES
    ('superadmin@company.com',
     '$2a$10$tggKyN3GsTRzQXBenrgd.elAd9SAe03/uBBQg0HJoJzX/h8XNykAi',
     'Super Admin', 'SUPER_ADMIN', 'ACTIVE');

CREATE TABLE refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    revoked_at  TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

CREATE TABLE uploaded_files (
    id            BIGSERIAL    PRIMARY KEY,
    filename      VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type  VARCHAR(100) NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    url           VARCHAR(500) NOT NULL,
    uploaded_by   BIGINT       REFERENCES users(id),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);
