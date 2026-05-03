-- V18__user_google_id.sql
-- Add Google OAuth identifier to users so the same email/account can sign in
-- via Google Sign-In once it has been linked.

ALTER TABLE users
    ADD COLUMN google_id VARCHAR(255);

CREATE UNIQUE INDEX idx_users_google_id ON users(google_id) WHERE google_id IS NOT NULL;
