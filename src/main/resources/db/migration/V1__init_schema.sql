-- GENRE
CREATE TABLE genre (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE,
    parent_id BIGINT REFERENCES genre(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_genre_name ON genre(name);
CREATE INDEX idx_genre_parent_id ON genre(parent_id);

-- AUTHOR
CREATE TABLE author (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sort_name VARCHAR(255),
    lang VARCHAR(10),
    biography TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_author_name ON author(name);

-- BOOK
CREATE TABLE book (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    annotation TEXT,
    language VARCHAR(10),
    pub_date VARCHAR(50),
    file_hash VARCHAR(64) UNIQUE,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_book_title ON book(title);
CREATE INDEX idx_book_available ON book(available);

-- BOOK_AUTHOR
CREATE TABLE book_author (
    book_id BIGINT NOT NULL REFERENCES book(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES author(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);

-- BOOK_GENRE
CREATE TABLE book_genre (
    book_id BIGINT NOT NULL REFERENCES book(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genre(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, genre_id)
);

-- BOOK_FILE
CREATE TABLE book_file (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES book(id) ON DELETE CASCADE,
    path VARCHAR(1000) NOT NULL,
    file_type VARCHAR(10),
    size BIGINT NOT NULL,
    hash VARCHAR(64),
    in_archive BOOLEAN NOT NULL DEFAULT FALSE,
    archive_path VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_book_file_book_id ON book_file(book_id);

-- USERS
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    telegram_id BIGINT UNIQUE,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- APP_SETTINGS
CREATE TABLE app_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(255) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_type VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- SCAN_HISTORY
CREATE TABLE scan_history (
    id BIGSERIAL PRIMARY KEY,
    scan_date TIMESTAMP NOT NULL DEFAULT NOW(),
    books_added INT NOT NULL DEFAULT 0,
    books_updated INT NOT NULL DEFAULT 0,
    books_deleted INT NOT NULL DEFAULT 0,
    duration_ms BIGINT,
    status VARCHAR(50),
    error_message TEXT
);

-- COVER_CACHE
CREATE TABLE cover_cache (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL UNIQUE,
    image_data BYTEA,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
