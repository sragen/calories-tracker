-- V3__app_configs.sql
-- Feature flags and remote config

CREATE TABLE app_configs (
    id          BIGSERIAL    PRIMARY KEY,
    key         VARCHAR(100) NOT NULL UNIQUE,
    value       TEXT         NOT NULL,
    type        VARCHAR(20)  NOT NULL DEFAULT 'BOOLEAN',
    label       VARCHAR(255),
    description TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_by  BIGINT       REFERENCES users(id)
);

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
     'URL gambar banner promo di home screen. Kosongkan untuk sembunyikan'),

    ('max_retry_login',
     '5', 'NUMBER',
     'Maks Percobaan Login',
     'Jumlah gagal login sebelum akun dikunci sementara');
