-- V7__subscription_permissions.sql

INSERT INTO role_permissions (role, module, can_read, can_write, can_delete) VALUES
    ('SUPER_ADMIN', 'SUBSCRIPTIONS', true, true,  true),
    ('SUPER_ADMIN', 'WHITELIST',     true, true,  true),
    ('ADMIN',       'SUBSCRIPTIONS', true, false, false);
