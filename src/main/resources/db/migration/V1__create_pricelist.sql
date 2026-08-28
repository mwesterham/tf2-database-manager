CREATE TABLE pricelist (
    sku             VARCHAR(100) PRIMARY KEY,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    autoprice       BOOLEAN      NOT NULL DEFAULT FALSE,
    intent          INTEGER      NOT NULL DEFAULT 2,  -- 0=buy 1=sell 2=bank
    min             INTEGER      NOT NULL DEFAULT 0,
    max             INTEGER      NOT NULL DEFAULT 1,
    buy_keys        INTEGER,
    buy_metal       NUMERIC(10, 4),
    sell_keys       INTEGER,
    sell_metal      NUMERIC(10, 4),
    note            TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pricelist_enabled ON pricelist (enabled);
CREATE INDEX idx_pricelist_intent  ON pricelist (intent);
