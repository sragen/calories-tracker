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
