-- V17__premium_yearly_plan.sql
-- Add Premium Yearly subscription plan (IDR 599,000 / year, ~50% savings vs Monthly).

INSERT INTO subscription_plans (name, price_idr, interval_days, trial_days, platform_product_id_android, platform_product_id_ios)
VALUES ('Premium Yearly', 599000, 365, 7, 'premium_yearly', 'premium_yearly');
