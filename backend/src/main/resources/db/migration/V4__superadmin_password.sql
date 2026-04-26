-- V4__superadmin_password.sql
-- Set superadmin default password to bcrypt("admin123")
-- GANTI SEGERA setelah deploy pertama ke environment baru!

UPDATE users
SET password = '$2a$10$tggKyN3GsTRzQXBenrgd.elAd9SAe03/uBBQg0HJoJzX/h8XNykAi'
WHERE email = 'superadmin@company.com';
