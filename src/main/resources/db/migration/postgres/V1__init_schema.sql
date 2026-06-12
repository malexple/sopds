-- Author
CREATE TABLE opds_catalog_author (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(128) NOT NULL,
    search_full_name VARCHAR(128) NOT NULL,
    lang_code INTEGER NOT NULL DEFAULT 9
);

CREATE INDEX idx_author_full_name ON opds_catalog_author(full_name);
CREATE INDEX idx_author_search_full_name ON opds_catalog_author(search_full_name);
CREATE INDEX idx_author_lang_code ON opds_catalog_author(lang_code);

-- Genre
CREATE TABLE opds_catalog_genre (
    id BIGSERIAL PRIMARY KEY,
    genre VARCHAR(32) NOT NULL,
    section VARCHAR(64) NOT NULL,
    subsection VARCHAR(100) NOT NULL
);

CREATE INDEX idx_genre_genre ON opds_catalog_genre(genre);
CREATE INDEX idx_genre_section ON opds_catalog_genre(section);
CREATE INDEX idx_genre_subsection ON opds_catalog_genre(subsection);

-- Series
CREATE TABLE opds_catalog_series (
    id BIGSERIAL PRIMARY KEY,
    ser VARCHAR(80) NOT NULL,
    search_ser VARCHAR(80) NOT NULL,
    lang_code INTEGER NOT NULL DEFAULT 9
);

CREATE INDEX idx_series_ser ON opds_catalog_series(ser);
CREATE INDEX idx_series_search_ser ON opds_catalog_series(search_ser);
CREATE INDEX idx_series_lang_code ON opds_catalog_series(lang_code);

-- Catalog
CREATE TABLE opds_catalog_catalog (
    id BIGSERIAL PRIMARY KEY,
    cat_name VARCHAR(128) NOT NULL,
    path VARCHAR(1000) NOT NULL,
    cat_type INTEGER NOT NULL DEFAULT 0,
    cat_size INTEGER DEFAULT 0,
    parent_id BIGINT REFERENCES opds_catalog_catalog(id) ON DELETE CASCADE
);

CREATE INDEX idx_catalog_cat_name ON opds_catalog_catalog(cat_name);
CREATE INDEX idx_catalog_path ON opds_catalog_catalog(path);

-- Book
CREATE TABLE opds_catalog_book (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(256) NOT NULL,
    path VARCHAR(1000) NOT NULL,
    filesize INTEGER NOT NULL DEFAULT 0,
    format VARCHAR(8) NOT NULL,
    cat_type INTEGER NOT NULL DEFAULT 0,
    registerdate TIMESTAMP NOT NULL DEFAULT NOW(),
    docdate VARCHAR(32),
    lang VARCHAR(16),
    title VARCHAR(256) NOT NULL,
    search_title VARCHAR(256) NOT NULL,
    annotation VARCHAR(10000),
    lang_code INTEGER NOT NULL DEFAULT 9,
    avail INTEGER NOT NULL DEFAULT 0,
    catalog_id BIGINT NOT NULL REFERENCES opds_catalog_catalog(id) ON DELETE CASCADE
);

CREATE INDEX idx_book_filename ON opds_catalog_book(filename);
CREATE INDEX idx_book_path ON opds_catalog_book(path);
CREATE INDEX idx_book_filesize ON opds_catalog_book(filesize);
CREATE INDEX idx_book_format ON opds_catalog_book(format);
CREATE INDEX idx_book_registerdate ON opds_catalog_book(registerdate);
CREATE INDEX idx_book_title ON opds_catalog_book(title);
CREATE INDEX idx_book_search_title ON opds_catalog_book(search_title);
CREATE INDEX idx_book_lang_code ON opds_catalog_book(lang_code);
CREATE INDEX idx_book_avail ON opds_catalog_book(avail);

-- Book-Author (Many-to-Many)
CREATE TABLE opds_catalog_bauthor (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES opds_catalog_book(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES opds_catalog_author(id) ON DELETE CASCADE
);

CREATE INDEX idx_bauthor_book_id ON opds_catalog_bauthor(book_id);
CREATE INDEX idx_bauthor_author_id ON opds_catalog_bauthor(author_id);

-- Book-Genre (Many-to-Many)
CREATE TABLE opds_catalog_bgenre (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES opds_catalog_book(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES opds_catalog_genre(id) ON DELETE CASCADE
);

CREATE INDEX idx_bgenre_book_id ON opds_catalog_bgenre(book_id);
CREATE INDEX idx_bgenre_genre_id ON opds_catalog_bgenre(genre_id);

-- Book-Series (Many-to-Many with ser_no)
CREATE TABLE opds_catalog_bseries (
    id BIGSERIAL PRIMARY KEY,
    ser_no INTEGER NOT NULL DEFAULT 0,
    book_id BIGINT NOT NULL REFERENCES opds_catalog_book(id) ON DELETE CASCADE,
    ser_id BIGINT NOT NULL REFERENCES opds_catalog_series(id) ON DELETE CASCADE
);

CREATE INDEX idx_bseries_book_id ON opds_catalog_bseries(book_id);
CREATE INDEX idx_bseries_ser_id ON opds_catalog_bseries(ser_id);

-- Bookshelf
CREATE TABLE opds_catalog_bookshelf (
    id BIGSERIAL PRIMARY KEY,
    readtime TIMESTAMP NOT NULL DEFAULT NOW(),
    book_id BIGINT NOT NULL REFERENCES opds_catalog_book(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL
);

CREATE INDEX idx_bookshelf_user_id ON opds_catalog_bookshelf(user_id);
CREATE INDEX idx_bookshelf_book_id ON opds_catalog_bookshelf(book_id);

-- Counter
CREATE TABLE IF NOT EXISTS opds_catalog_counter (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE,
    value INTEGER NOT NULL DEFAULT 0
);

-- Auth User (Django compatible)
CREATE TABLE auth_user (
    id BIGSERIAL PRIMARY KEY,
    password VARCHAR(128) NOT NULL,
    last_login TIMESTAMP,
    is_superuser BOOLEAN NOT NULL DEFAULT FALSE,
    username VARCHAR(150) NOT NULL UNIQUE,
    first_name VARCHAR(150) DEFAULT '',
    last_name VARCHAR(150) DEFAULT '',
    email VARCHAR(254) DEFAULT '',
    is_staff BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_joined TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auth_user_username ON auth_user(username);

-- Constance Config (Django settings)
CREATE TABLE constance_config (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(255) NOT NULL UNIQUE,
    value TEXT
);

CREATE INDEX idx_constance_config_key ON constance_config(key);
