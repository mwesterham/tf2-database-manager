-- Generic key-value store for bot settings (e.g. poll offer_since timestamp).
CREATE TABLE settings (
    key    VARCHAR(100) PRIMARY KEY,
    value  TEXT
);

-- Seed poll cursor.
INSERT INTO settings (key, value) VALUES ('offer_since', '0');
