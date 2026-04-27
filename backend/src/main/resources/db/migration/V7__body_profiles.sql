CREATE TABLE body_profiles (
    id                   BIGSERIAL    PRIMARY KEY,
    user_id              BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    height_cm            NUMERIC(5,1) NOT NULL,
    weight_kg            NUMERIC(5,1) NOT NULL,
    birth_date           DATE         NOT NULL,
    gender               VARCHAR(10)  NOT NULL,
    activity_level       VARCHAR(25)  NOT NULL DEFAULT 'SEDENTARY',
    goal                 VARCHAR(20)  NOT NULL DEFAULT 'MAINTAIN',
    target_weight_kg     NUMERIC(5,1),
    bmr_kcal             NUMERIC(8,2),
    tdee_kcal            NUMERIC(8,2),
    recommended_calories NUMERIC(8,2),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_body_profiles_user ON body_profiles(user_id);
