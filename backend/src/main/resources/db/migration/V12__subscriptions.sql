-- Subscription Plans
CREATE TABLE subscription_plans (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    price_idr   BIGINT NOT NULL,
    duration_days INT NOT NULL,
    features    TEXT,           -- JSON array stored as text
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- User Subscriptions
CREATE TABLE user_subscriptions (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id),
    plan_id        BIGINT NOT NULL REFERENCES subscription_plans(id),
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING | ACTIVE | EXPIRED | CANCELLED
    started_at     TIMESTAMP,
    expires_at     TIMESTAMP,
    payment_id     VARCHAR(255),
    payment_method VARCHAR(100),
    snap_token     VARCHAR(500),
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_subscriptions_user ON user_subscriptions(user_id);
CREATE INDEX idx_user_subscriptions_status ON user_subscriptions(status);

-- AI Scan Logs (for usage tracking and debugging)
CREATE TABLE ai_scan_logs (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id),
    image_url      VARCHAR(500),
    raw_response   TEXT,
    detected_count INT NOT NULL DEFAULT 0,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_scan_logs_user ON ai_scan_logs(user_id);

-- Seed subscription plans
INSERT INTO subscription_plans (name, description, price_idr, duration_days, features) VALUES
('Premium Bulanan', 'Akses penuh fitur AI scan makanan dan analitik lanjutan', 29000, 30,
 '["ai_scan","advanced_analytics","export_data","unlimited_logs"]'),
('Premium Tahunan', 'Hemat 57%! Akses semua fitur premium selama 1 tahun', 149000, 365,
 '["ai_scan","advanced_analytics","export_data","unlimited_logs","priority_support"]');
