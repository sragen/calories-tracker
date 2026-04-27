CREATE TABLE meal_logs (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    food_item_id        BIGINT       NOT NULL REFERENCES food_items(id),
    quantity_g          NUMERIC(8,2) NOT NULL,
    meal_type           VARCHAR(20)  NOT NULL DEFAULT 'SNACK',
    calories_snapshot   NUMERIC(8,2) NOT NULL,
    protein_g_snapshot  NUMERIC(8,2) NOT NULL DEFAULT 0,
    carbs_g_snapshot    NUMERIC(8,2) NOT NULL DEFAULT 0,
    fat_g_snapshot      NUMERIC(8,2) NOT NULL DEFAULT 0,
    ai_scan_photo_url   VARCHAR(500),
    ai_confidence       NUMERIC(4,3),
    logged_at           DATE         NOT NULL DEFAULT CURRENT_DATE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP
);

CREATE INDEX idx_meal_logs_user_date ON meal_logs(user_id, logged_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_meal_logs_food      ON meal_logs(food_item_id)        WHERE deleted_at IS NULL;
