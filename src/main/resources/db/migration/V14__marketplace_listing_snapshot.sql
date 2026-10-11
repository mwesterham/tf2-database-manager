DROP TABLE IF EXISTS trade_event_bptf_listings;

CREATE TABLE trade_event_bptf_listings (
    id                   BIGSERIAL         PRIMARY KEY,
    trade_event_id       BIGINT            NOT NULL REFERENCES trade_events(id) ON DELETE CASCADE,
    sku                  VARCHAR(64)       NOT NULL,
    intent               VARCHAR(4)        NOT NULL,
    steam_id             VARCHAR(32),
    user_name            TEXT,
    raw_value            DOUBLE PRECISION,
    metal                DOUBLE PRECISION,
    keys                 BIGINT            NOT NULL DEFAULT 0,
    market_name          TEXT,
    details              TEXT,
    listed_at            BIGINT,
    user_premium         BOOLEAN,
    user_online          BOOLEAN,
    user_banned          BOOLEAN,
    user_trade_offer_url TEXT
);

CREATE INDEX idx_tebl_event_id ON trade_event_bptf_listings(trade_event_id);
