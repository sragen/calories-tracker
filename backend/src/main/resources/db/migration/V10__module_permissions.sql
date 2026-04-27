INSERT INTO role_permissions (role, module, can_read, can_write, can_delete) VALUES
    ('SUPER_ADMIN', 'FOODS',     TRUE, TRUE,  TRUE),
    ('SUPER_ADMIN', 'MEAL_LOGS', TRUE, TRUE,  TRUE),
    ('ADMIN',       'FOODS',     TRUE, TRUE,  FALSE),
    ('ADMIN',       'MEAL_LOGS', TRUE, FALSE, FALSE),
    ('STAFF',       'FOODS',     TRUE, FALSE, FALSE);
