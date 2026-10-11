ALTER TABLE trade_event_items
    ADD COLUMN used_price_refined DOUBLE PRECISION NOT NULL DEFAULT 0;

CREATE TABLE trade_event_snapshots (
    id                 BIGSERIAL        PRIMARY KEY,
    trade_event_id     BIGINT           NOT NULL REFERENCES trade_events(id) ON DELETE CASCADE,
    sku                VARCHAR(64)      NOT NULL,
    buy_price_refined  DOUBLE PRECISION NOT NULL DEFAULT 0,
    sell_price_refined DOUBLE PRECISION NOT NULL DEFAULT 0,
    intent             VARCHAR(8)       NOT NULL DEFAULT 'bank'
);

CREATE INDEX idx_tes_event_id ON trade_event_snapshots(trade_event_id);
