-- Create Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(254),
    first_name VARCHAR(150),
    last_name VARCHAR(150),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_staff BOOLEAN NOT NULL DEFAULT FALSE,
    is_superuser BOOLEAN NOT NULL DEFAULT FALSE,
    telegram_username VARCHAR(100),
    last_login TIMESTAMP,
    date_joined TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_telegram ON users(telegram_username);

-- Create Groups table
CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT
);

CREATE INDEX idx_group_name ON groups(name);

-- Create User-Group junction table
CREATE TABLE user_groups (
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
);

-- Create Group Permissions table
CREATE TABLE group_permissions (
    group_id BIGINT NOT NULL,
    permission VARCHAR(255) NOT NULL,
    PRIMARY KEY (group_id, permission),
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
);

-- Create Authors table
CREATE TABLE authors (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    middle_name VARCHAR(100),
    last_name VARCHAR(100),
    full_name_sort VARCHAR(255)
);

CREATE INDEX idx_author_full_name ON authors(full_name);
CREATE INDEX idx_author_last_name ON authors(last_name);

-- Create Series table
CREATE TABLE series (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    name_sort VARCHAR(500)
);

CREATE INDEX idx_series_name ON series(name);

-- Create Genres table
CREATE TABLE genres (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name_ru VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    description TEXT,
    parent_id BIGINT,
    FOREIGN KEY (parent_id) REFERENCES genres(id) ON DELETE SET NULL
);

CREATE INDEX idx_genre_code ON genres(code);

-- Create Books table
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    title_sort VARCHAR(500),
    annotation TEXT,
    isbn VARCHAR(13),
    lang VARCHAR(10),
    path VARCHAR(1000) NOT NULL,
    filename VARCHAR(500),
    format VARCHAR(10),
    filesize NUMERIC(10, 2),
    publish_date DATE,
    register_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    is_duplicate BOOLEAN NOT NULL DEFAULT FALSE,
    page_count INTEGER,
    md5 VARCHAR(32),
    series_id BIGINT,
    series_number INTEGER,
    FOREIGN KEY (series_id) REFERENCES series(id) ON DELETE SET NULL
);

CREATE INDEX idx_book_title ON books(title);
CREATE INDEX idx_book_path ON books(path);
CREATE INDEX idx_book_format ON books(format);
CREATE INDEX idx_book_reg_date ON books(register_date);
CREATE INDEX idx_book_available ON books(available);

-- Create Book-Author junction table
CREATE TABLE book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

-- Create Book-Genre junction table
CREATE TABLE book_genres (
    book_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, genre_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);