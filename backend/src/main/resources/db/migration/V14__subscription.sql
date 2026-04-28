-- V5__subscription.sql

CREATE TABLE subscription_plans (
    id                          BIGSERIAL     PRIMARY KEY,
    name                        VARCHAR(100)  NOT NULL,
    price_idr                   BIGINT        NOT NULL,
    interval_days               INT           NOT NULL DEFAULT 30,
    trial_days                  INT           NOT NULL DEFAULT 7,
    platform_product_id_android VARCHAR(200),
    platform_product_id_ios     VARCHAR(200),
    is_active                   BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP     NOT NULL DEFAULT NOW()
);

INSERT INTO subscription_plans (name, price_idr, interval_days, trial_days, platform_product_id_android, platform_product_id_ios)
VALUES ('Premium Monthly', 99000, 30, 7, 'premium_monthly', 'premium_monthly');

CREATE TABLE subscriptions (
    id                                  BIGSERIAL     PRIMARY KEY,
    user_id                             BIGINT        NOT NULL REFERENCES users(id),
    plan_id                             BIGINT        NOT NULL REFERENCES subscription_plans(id),
    status                              VARCHAR(50)   NOT NULL,
    platform                            VARCHAR(20)   NOT NULL,
    platform_purchase_token             VARCHAR(1000),
    platform_original_transaction_id    VARCHAR(500),
    platform_order_id                   VARCHAR(500)  UNIQUE,
    trial_ends_at                       TIMESTAMP,
    current_period_start                TIMESTAMP,
    current_period_end                  TIMESTAMP,
    grace_period_ends_at                TIMESTAMP,
    cancelled_at                        TIMESTAMP,
    created_at                          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at                          TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at                          TIMESTAMP
);

CREATE UNIQUE INDEX idx_subscriptions_user_active
    ON subscriptions(user_id)
    WHERE status IN ('TRIAL', 'ACTIVE', 'PAST_DUE') AND deleted_at IS NULL;

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_status  ON subscriptions(status);
CREATE INDEX idx_subscriptions_token   ON subscriptions(platform_purchase_token);

CREATE TABLE payment_events (
    id                BIGSERIAL    PRIMARY KEY,
    subscription_id   BIGINT       REFERENCES subscriptions(id),
    user_id           BIGINT       REFERENCES users(id),
    platform          VARCHAR(20)  NOT NULL,
    event_type        VARCHAR(100) NOT NULL,
    platform_order_id VARCHAR(500),
    raw_payload       TEXT,
    processed_at      TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_events_subscription_id ON payment_events(subscription_id);
CREATE INDEX idx_payment_events_user_id         ON payment_events(user_id);

-- subscriptions: premium entitlement check (most frequent auth gate)
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_status ON subscriptions(user_id, status, current_period_end);
