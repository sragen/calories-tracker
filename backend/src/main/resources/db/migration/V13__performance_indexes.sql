-- Performance indexes for high-frequency queries

-- meal_logs: primary access pattern is by user + date
CREATE INDEX IF NOT EXISTS idx_meal_logs_user_date ON meal_logs(user_id, logged_at DESC);

-- meal_logs: analytics top-foods join
CREATE INDEX IF NOT EXISTS idx_meal_logs_food_item ON meal_logs(food_item_id);

-- ai_scan_logs: rate-limit query (user + date)
CREATE INDEX IF NOT EXISTS idx_ai_scan_logs_user_created ON ai_scan_logs(user_id, created_at DESC);

-- users: login lookup
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email) WHERE deleted_at IS NULL;
