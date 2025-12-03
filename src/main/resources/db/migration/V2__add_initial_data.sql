-- Начальные жанры
INSERT INTO opds_catalog_genre (genre, section, subsection) VALUES
('sf', 'Фантастика', 'Научная фантастика'),
('fantasy', 'Фантастика', 'Фэнтези'),
('detective', 'Детективы', 'Детектив'),
('prose', 'Проза', 'Современная проза'),
('romance', 'Любовные романы', 'Любовный роман');

-- Начальный каталог
INSERT INTO opds_catalog_catalog (cat_name, path, cat_type, cat_size) VALUES
('Root', '/', 0, 0);

-- Начальные счётчики
INSERT INTO opds_catalog_counter (name, value, update_time) VALUES
('books', 0, NOW()),
('authors', 0, NOW()),
('catalogs', 0, NOW());

-- Admin пользователь (пароль: admin)
INSERT INTO auth_user (password, is_superuser, username, is_staff, is_active, date_joined) VALUES
('pbkdf2_sha256$30000$salt$hashedpassword', TRUE, 'admin', TRUE, TRUE, NOW());

-- Начальные настройки
INSERT INTO constance_config (key, value) VALUES
('SOPDS_ROOT_LIB', '/var/lib/sopds/books'),
('SOPDS_LANGUAGE', 'ru'),
('SOPDS_MAXITEMS', '60');
