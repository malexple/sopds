CREATE TABLE IF NOT EXISTS opds_catalog_author (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name VARCHAR(128) NOT NULL,
    search_full_name VARCHAR(128) NOT NULL,
    lang_code INTEGER NOT NULL DEFAULT 9
);
CREATE INDEX IF NOT EXISTS idx_author_full_name ON opds_catalog_author(full_name);
CREATE INDEX IF NOT EXISTS idx_author_search_full_name ON opds_catalog_author(search_full_name);
CREATE INDEX IF NOT EXISTS idx_author_lang_code ON opds_catalog_author(lang_code);

CREATE TABLE IF NOT EXISTS opds_catalog_genre (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    genre VARCHAR(32) NOT NULL,
    section VARCHAR(64) NOT NULL,
    subsection VARCHAR(100) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_genre_genre ON opds_catalog_genre(genre);
CREATE INDEX IF NOT EXISTS idx_genre_section ON opds_catalog_genre(section);
CREATE INDEX IF NOT EXISTS idx_genre_subsection ON opds_catalog_genre(subsection);

CREATE TABLE IF NOT EXISTS opds_catalog_series (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ser VARCHAR(80) NOT NULL,
    search_ser VARCHAR(80) NOT NULL,
    lang_code INTEGER NOT NULL DEFAULT 9
);
CREATE INDEX IF NOT EXISTS idx_series_ser ON opds_catalog_series(ser);
CREATE INDEX IF NOT EXISTS idx_series_search_ser ON opds_catalog_series(search_ser);
CREATE INDEX IF NOT EXISTS idx_series_lang_code ON opds_catalog_series(lang_code);

CREATE TABLE IF NOT EXISTS opds_catalog_catalog (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cat_name VARCHAR(128) NOT NULL,
    path VARCHAR(1000) NOT NULL,
    cat_type INTEGER NOT NULL DEFAULT 0,
    cat_size INTEGER DEFAULT 0,
    parent_id INTEGER REFERENCES opds_catalog_catalog(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_catalog_cat_name ON opds_catalog_catalog(cat_name);
CREATE INDEX IF NOT EXISTS idx_catalog_path ON opds_catalog_catalog(path);

CREATE TABLE IF NOT EXISTS opds_catalog_book (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    filename VARCHAR(256) NOT NULL,
    path VARCHAR(1000) NOT NULL,
    filesize INTEGER NOT NULL DEFAULT 0,
    format VARCHAR(8) NOT NULL,
    cat_type INTEGER NOT NULL DEFAULT 0,
    registerdate DATETIME NOT NULL DEFAULT (datetime('now')),
    docdate VARCHAR(32),
    lang VARCHAR(16),
    title VARCHAR(256) NOT NULL,
    search_title VARCHAR(256) NOT NULL,
    annotation VARCHAR(10000),
    lang_code INTEGER NOT NULL DEFAULT 9,
    avail INTEGER NOT NULL DEFAULT 0,
    catalog_id INTEGER NOT NULL REFERENCES opds_catalog_catalog(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_book_filename ON opds_catalog_book(filename);
CREATE INDEX IF NOT EXISTS idx_book_path ON opds_catalog_book(path);
CREATE INDEX IF NOT EXISTS idx_book_filesize ON opds_catalog_book(filesize);
CREATE INDEX IF NOT EXISTS idx_book_format ON opds_catalog_book(format);
CREATE INDEX IF NOT EXISTS idx_book_registerdate ON opds_catalog_book(registerdate);
CREATE INDEX IF NOT EXISTS idx_book_title ON opds_catalog_book(title);
CREATE INDEX IF NOT EXISTS idx_book_search_title ON opds_catalog_book(search_title);
CREATE INDEX IF NOT EXISTS idx_book_lang_code ON opds_catalog_book(lang_code);
CREATE INDEX IF NOT EXISTS idx_book_avail ON opds_catalog_book(avail);

CREATE TABLE IF NOT EXISTS opds_catalog_bauthor (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id INTEGER NOT NULL REFERENCES opds_catalog_book(id) ON DELETE CASCADE,
    author_id INTEGER NOT NULL REFERENCES opds_catalog_author(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_bauthor_book_id ON opds_catalog_bauthor(book_id);
CREATE INDEX IF NOT EXISTS idx_bauthor_author_id ON opds_catalog_bauthor(author_id);

CREATE TABLE IF NOT EXISTS opds_catalog_bgenre (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id INTEGER NOT NULL REFERENCES opds_catalog_book(id) ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES opds_catalog_genre(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_bgenre_book_id ON opds_catalog_bgenre(book_id);
CREATE INDEX IF NOT EXISTS idx_bgenre_genre_id ON opds_catalog_bgenre(genre_id);

CREATE TABLE IF NOT EXISTS opds_catalog_bseries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ser_no INTEGER NOT NULL DEFAULT 0,
    book_id INTEGER NOT NULL REFERENCES opds_catalog_book(id) ON DELETE CASCADE,
    ser_id INTEGER NOT NULL REFERENCES opds_catalog_series(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_bseries_book_id ON opds_catalog_bseries(book_id);
CREATE INDEX IF NOT EXISTS idx_bseries_ser_id ON opds_catalog_bseries(ser_id);

CREATE TABLE IF NOT EXISTS opds_catalog_bookshelf (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    readtime DATETIME NOT NULL DEFAULT (datetime('now')),
    book_id INTEGER NOT NULL REFERENCES opds_catalog_book(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_bookshelf_user_id ON opds_catalog_bookshelf(user_id);
CREATE INDEX IF NOT EXISTS idx_bookshelf_book_id ON opds_catalog_bookshelf(book_id);

CREATE TABLE IF NOT EXISTS opds_catalog_counter (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(32) NOT NULL UNIQUE,
    value INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS auth_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    password VARCHAR(128) NOT NULL,
    last_login DATETIME,
    is_superuser INTEGER NOT NULL DEFAULT 0,
    username VARCHAR(150) NOT NULL UNIQUE,
    first_name VARCHAR(150) DEFAULT '',
    last_name VARCHAR(150) DEFAULT '',
    email VARCHAR(254) DEFAULT '',
    is_staff INTEGER NOT NULL DEFAULT 0,
    is_active INTEGER NOT NULL DEFAULT 1,
    date_joined DATETIME NOT NULL DEFAULT (datetime('now'))
);
CREATE INDEX IF NOT EXISTS idx_auth_user_username ON auth_user(username);

CREATE TABLE IF NOT EXISTS constance_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    key VARCHAR(255) NOT NULL UNIQUE,
    value TEXT
);
CREATE INDEX IF NOT EXISTS idx_constance_config_key ON constance_config(key);