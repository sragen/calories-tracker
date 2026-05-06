CREATE TABLE user_food_library (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    food_name         VARCHAR(255) NOT NULL,
    calories_per_100g NUMERIC(8,2) NOT NULL,
    protein_per_100g  NUMERIC(6,2) NOT NULL DEFAULT 0,
    carbs_per_100g    NUMERIC(6,2) NOT NULL DEFAULT 0,
    fat_per_100g      NUMERIC(6,2) NOT NULL DEFAULT 0,
    serving_size_g    NUMERIC(6,2) NOT NULL DEFAULT 100,
    image_url         VARCHAR(512),
    source            VARCHAR(50) NOT NULL DEFAULT 'AI_SCAN',
    original_food_id  BIGINT REFERENCES food_items(id),
    use_count         INTEGER NOT NULL DEFAULT 0,
    last_used_at      TIMESTAMP,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMP
);

CREATE INDEX idx_ufl_user_id          ON user_food_library(user_id);
CREATE INDEX idx_ufl_last_used        ON user_food_library(user_id, last_used_at DESC NULLS LAST) WHERE deleted_at IS NULL;
CREATE INDEX idx_ufl_use_count        ON user_food_library(user_id, use_count DESC)              WHERE deleted_at IS NULL;
CREATE INDEX idx_ufl_food_name        ON user_food_library(user_id, food_name)                   WHERE deleted_at IS NULL;
