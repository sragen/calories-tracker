INSERT INTO app_configs (key, value, type, label, description) VALUES
    ('ai_scan_enabled',       'true',  'BOOLEAN', 'AI Scan Aktif',          'Toggle fitur AI foto scan makanan'),
    ('free_daily_log_limit',  '10',    'NUMBER',  'Limit Log Harian Free',  'Jumlah log per hari untuk user free tier'),
    ('barcode_scan_enabled',  'true',  'BOOLEAN', 'Barcode Scan Aktif',     'Toggle barcode scan Open Food Facts'),
    ('payment_enabled',       'false', 'BOOLEAN', 'Payment Gateway',        'Aktifkan payment gateway Midtrans');
