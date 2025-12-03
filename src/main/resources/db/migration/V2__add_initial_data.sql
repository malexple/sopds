-- Начальные жанры
INSERT INTO opds_catalog_genre (genre, section, subsection) VALUES
('sf', 'Фантастика', 'Научная фантастика'),
('fantasy', 'Фантастика', 'Фэнтези'),
('detective', 'Детективы', 'Детектив'),
('prose', 'Проза', 'Современная проза'),
('romance', 'Любовные романы', 'Любовный роман');

-- Начальный каталог
INSERT INTO opds_catalog_catalog (cat_name, path, cat_type, cat_size) VALUES
('Root', '/', 0, 0)
ON CONFLICT DO NOTHING;

-- Начальные счётчики
INSERT INTO opds_catalog_counter (name, value, update_time) VALUES
('books', 0, NOW()),
('authors', 0, NOW()),
('catalogs', 0, NOW())
ON CONFLICT DO NOTHING;

-- Начальные настройки
INSERT INTO constance_config (key, value) VALUES
('SOPDS_ROOT_LIB', '/var/lib/sopds/books'),
('SOPDS_LANGUAGE', 'ru'),
('SOPDS_MAXITEMS', '60'),
('SOPDS_BOOK_EXTENSIONS', 'fb2,epub,mobi,pdf,djvu,doc,docx,rtf,txt')
ON CONFLICT (key) DO NOTHING;