-- V6__premium_whitelist.sql

CREATE TABLE premium_whitelists (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL UNIQUE REFERENCES users(id),
    added_by    BIGINT      NOT NULL REFERENCES users(id),
    note        TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_premium_whitelists_user_id ON premium_whitelists(user_id);
