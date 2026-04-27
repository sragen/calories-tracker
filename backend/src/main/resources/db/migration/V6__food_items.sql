CREATE TABLE food_items (
    id                  BIGSERIAL     PRIMARY KEY,
    name                VARCHAR(255)  NOT NULL,
    name_en             VARCHAR(255),
    category_id         BIGINT        REFERENCES food_categories(id),
    calories_per_100g   NUMERIC(8,2)  NOT NULL,
    protein_per_100g    NUMERIC(8,2)  NOT NULL DEFAULT 0,
    carbs_per_100g      NUMERIC(8,2)  NOT NULL DEFAULT 0,
    fat_per_100g        NUMERIC(8,2)  NOT NULL DEFAULT 0,
    fiber_per_100g      NUMERIC(8,2)  DEFAULT 0,
    sugar_per_100g      NUMERIC(8,2)  DEFAULT 0,
    sodium_per_100mg    NUMERIC(8,2)  DEFAULT 0,
    default_serving_g   NUMERIC(8,2)  NOT NULL DEFAULT 100,
    serving_description VARCHAR(100),
    barcode             VARCHAR(50)   UNIQUE,
    source              VARCHAR(20)   NOT NULL DEFAULT 'ADMIN',
    external_id         VARCHAR(100),
    is_verified         BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by          BIGINT        REFERENCES users(id),
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP
);

CREATE INDEX idx_food_items_name     ON food_items(name)       WHERE deleted_at IS NULL;
CREATE INDEX idx_food_items_name_en  ON food_items(name_en)    WHERE deleted_at IS NULL;
CREATE INDEX idx_food_items_barcode  ON food_items(barcode)    WHERE deleted_at IS NULL AND barcode IS NOT NULL;
CREATE INDEX idx_food_items_category ON food_items(category_id) WHERE deleted_at IS NULL AND is_active = TRUE;
CREATE INDEX idx_food_items_source   ON food_items(source)     WHERE deleted_at IS NULL;

-- Seed: 20 makanan Indonesia populer (sumber: TKPI 2017)
INSERT INTO food_items (name, name_en, category_id, calories_per_100g, protein_per_100g, carbs_per_100g, fat_per_100g, fiber_per_100g, default_serving_g, serving_description, source, is_verified) VALUES
    ('Nasi Putih',       'Steamed White Rice',    1, 175, 3.1, 39.8, 0.2, 0.2, 200, '1 centong (200g)',       'TKPI', TRUE),
    ('Nasi Goreng',      'Fried Rice',            1, 182, 4.7, 37.2, 2.1, 0.5, 250, '1 porsi (250g)',         'TKPI', TRUE),
    ('Nasi Uduk',        'Coconut Rice',          1, 155, 3.5, 32.1, 1.8, 0.3, 200, '1 centong (200g)',       'TKPI', TRUE),
    ('Mie Goreng',       'Fried Noodles',         1, 208, 6.1, 35.2, 5.8, 0.8, 200, '1 porsi (200g)',         'TKPI', TRUE),
    ('Roti Tawar',       'White Bread',           1, 248, 7.9, 50.6, 1.2, 2.4, 30,  '1 lembar (30g)',         'TKPI', TRUE),
    ('Ayam Goreng',      'Fried Chicken',         2, 260, 27.4, 0.0, 16.8, 0.0, 80, '1 potong paha (80g)',    'TKPI', TRUE),
    ('Tempe Goreng',     'Fried Tempeh',          2, 227, 18.3, 9.4, 14.2, 4.1, 50, '2 potong (50g)',         'TKPI', TRUE),
    ('Tahu Goreng',      'Fried Tofu',            2, 194, 12.6, 3.8, 14.9, 0.3, 60, '1 potong besar (60g)',   'TKPI', TRUE),
    ('Rendang Sapi',     'Beef Rendang',          2, 193, 19.4, 5.5, 10.3, 0.5, 100,'1 porsi (100g)',         'TKPI', TRUE),
    ('Telur Ayam Rebus', 'Boiled Egg',            2, 162, 12.4, 0.7, 11.5, 0.0, 60, '1 butir besar (60g)',    'TKPI', TRUE),
    ('Gado-gado',        'Peanut Sauce Salad',    2, 95,  4.2, 11.3, 3.9, 2.1, 200, '1 porsi (200g)',         'TKPI', TRUE),
    ('Soto Ayam',        'Chicken Soto Soup',     2, 72,  9.1, 3.8, 2.4, 0.3, 300, '1 mangkuk (300g)',        'TKPI', TRUE),
    ('Bayam Rebus',      'Boiled Spinach',        3, 20,  2.3, 3.2, 0.3, 2.8, 100, '1 porsi (100g)',          'TKPI', TRUE),
    ('Kangkung Tumis',   'Stir-fried Water Spinach', 3, 44, 4.0, 6.2, 0.7, 2.5, 100, '1 porsi (100g)',       'TKPI', TRUE),
    ('Pisang Ambon',     'Banana',                4, 99,  1.2, 25.8, 0.2, 0.7, 100, '1 buah sedang (100g)',   'TKPI', TRUE),
    ('Apel Merah',       'Red Apple',             4, 58,  0.3, 14.9, 0.2, 2.4, 150, '1 buah sedang (150g)',   'TKPI', TRUE),
    ('Teh Manis',        'Sweet Tea',             5, 50,  0.0, 12.5, 0.0, 0.0, 250, '1 gelas (250ml)',        'TKPI', TRUE),
    ('Kopi Hitam',       'Black Coffee',          5,  2,  0.3, 0.0, 0.0, 0.0, 200, '1 cangkir (200ml)',      'TKPI', TRUE),
    ('Susu Sapi',        'Fresh Cow Milk',        9, 61,  3.2, 4.3, 3.5, 0.0, 250, '1 gelas (250ml)',        'TKPI', TRUE),
    ('Indomie Goreng',   'Indomie Fried Noodles', 8, 422, 9.5, 68.3, 13.1, 2.2, 85, '1 bungkus (85g)',       'ADMIN', TRUE);
