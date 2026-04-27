CREATE TABLE food_categories (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    name_en    VARCHAR(100),
    icon       VARCHAR(10),
    sort_order INTEGER      NOT NULL DEFAULT 0,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO food_categories (name, name_en, icon, sort_order) VALUES
    ('Nasi & Sereal',      'Rice & Grains',      '🍚', 1),
    ('Lauk Pauk',          'Side Dishes',        '🍖', 2),
    ('Sayuran',            'Vegetables',         '🥦', 3),
    ('Buah-buahan',        'Fruits',             '🍎', 4),
    ('Minuman',            'Beverages',          '🥤', 5),
    ('Snack & Camilan',    'Snacks',             '🍪', 6),
    ('Makanan Cepat Saji', 'Fast Food',          '🍔', 7),
    ('Produk Kemasan',     'Packaged Products',  '📦', 8),
    ('Olahan Susu',        'Dairy',              '🥛', 9),
    ('Masakan Indonesia',  'Indonesian Cuisine', '🍽️', 10),
    ('Lainnya',            'Others',             '❓', 99);
