-- V2__role_permissions.sql
-- Permission per role per module

CREATE TABLE role_permissions (
    role        VARCHAR(50) NOT NULL REFERENCES roles(name),
    module      VARCHAR(50) NOT NULL,
    can_read    BOOLEAN     NOT NULL DEFAULT TRUE,
    can_write   BOOLEAN     NOT NULL DEFAULT FALSE,
    can_delete  BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (role, module)
);

INSERT INTO role_permissions (role, module, can_read, can_write, can_delete) VALUES
    -- SUPER_ADMIN: full access
    ('SUPER_ADMIN', 'USERS',   true, true, true),
    ('SUPER_ADMIN', 'CONFIG',  true, true, true),
    ('SUPER_ADMIN', 'FILES',   true, true, true),
    ('SUPER_ADMIN', 'REPORTS', true, true, true),

    -- ADMIN: read+write, no delete
    ('ADMIN', 'USERS',   true, true,  false),
    ('ADMIN', 'CONFIG',  true, true,  false),
    ('ADMIN', 'FILES',   true, true,  false),
    ('ADMIN', 'REPORTS', true, false, false),

    -- STAFF: read-only on USERS and REPORTS
    ('STAFF', 'USERS',   true,  false, false),
    ('STAFF', 'CONFIG',  false, false, false),
    ('STAFF', 'FILES',   false, false, false),
    ('STAFF', 'REPORTS', true,  false, false);

-- Cara tambah modul baru (contoh: PRODUCTS) — buat migration baru:
-- INSERT INTO role_permissions (role, module, can_read, can_write, can_delete) VALUES
--     ('SUPER_ADMIN', 'PRODUCTS', true,  true,  true),
--     ('ADMIN',       'PRODUCTS', true,  true,  false),
--     ('STAFF',       'PRODUCTS', true,  false, false);
