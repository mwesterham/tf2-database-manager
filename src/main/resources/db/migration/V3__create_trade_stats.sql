-- Single-row table (id always = 1).
CREATE TABLE trade_stats (
    id                      INTEGER      PRIMARY KEY DEFAULT 1,
    since                   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    trades_accepted         INTEGER      NOT NULL DEFAULT 0,
    trades_declined         INTEGER      NOT NULL DEFAULT 0,
    total_profit_in_refined NUMERIC(12, 4) NOT NULL DEFAULT 0,
    CONSTRAINT single_row CHECK (id = 1)
);

-- Seed the single row so it always exists.
INSERT INTO trade_stats (id, since) VALUES (1, NOW());
