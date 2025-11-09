-- Increase ISBN column length to handle various ISBN formats
-- ISBN-10: 10 digits
-- ISBN-13: 13 digits
-- But some FB2 files have invalid/extended ISBN values
ALTER TABLE books ALTER COLUMN isbn TYPE VARCHAR(50);