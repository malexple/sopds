-- Начальные жанры
INSERT INTO genre (name, code) VALUES
('Fiction', 'fiction'),
('Non-Fiction', 'non_fiction'),
('Science Fiction', 'sci_fi'),
('Fantasy', 'fantasy'),
('Mystery', 'mystery')
ON CONFLICT DO NOTHING;

-- Admin пользователь (пароль: admin, BCrypt hash)
INSERT INTO users (username, password_hash, is_admin, is_active) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye1IDQH00.vCX8L8Q1Z9qI8S7JtqXCr.i', TRUE, TRUE)
ON CONFLICT (username) DO NOTHING;

-- Начальные настройки
INSERT INTO app_settings (setting_key, setting_value, setting_type) VALUES
('SOPDS_ROOT_LIB', '/var/lib/sopds/books', 'STRING'),
('SOPDS_LANGUAGE', 'en-EN', 'STRING'),
('SOPDS_AUTH', 'true', 'BOOLEAN'),
('SOPDS_MAXITEMS', '60', 'INT')
ON CONFLICT (setting_key) DO NOTHING;
