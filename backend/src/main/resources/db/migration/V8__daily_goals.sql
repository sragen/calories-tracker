CREATE TABLE daily_goals (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    target_calories  NUMERIC(8,2) NOT NULL,
    target_protein_g NUMERIC(8,2) NOT NULL DEFAULT 0,
    target_carbs_g   NUMERIC(8,2) NOT NULL DEFAULT 0,
    target_fat_g     NUMERIC(8,2) NOT NULL DEFAULT 0,
    auto_calculated  BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);
