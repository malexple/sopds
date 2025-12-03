-- ============================================
-- COVER_CACHE TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS cover_cache (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL UNIQUE,
    image_data BYTEA,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_cover_book FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE
);

CREATE INDEX idx_cover_cache_book_id ON cover_cache(book_id);
