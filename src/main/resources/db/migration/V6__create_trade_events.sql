CREATE TABLE trade_events (
    id                  BIGSERIAL    PRIMARY KEY,
    offer_id            VARCHAR(64)  NOT NULL,
    partner_steam_id64  VARCHAR(32),
    profit_in_refined   DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    traded_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE trade_event_items (
    id               BIGSERIAL   PRIMARY KEY,
    trade_event_id   BIGINT      NOT NULL REFERENCES trade_events(id) ON DELETE CASCADE,
    sku              VARCHAR(64) NOT NULL,
    direction        VARCHAR(8)  NOT NULL
);

CREATE INDEX idx_tei_sku      ON trade_event_items(sku);
CREATE INDEX idx_tei_event_id ON trade_event_items(trade_event_id);

DROP TABLE IF EXISTS trade_stats;
