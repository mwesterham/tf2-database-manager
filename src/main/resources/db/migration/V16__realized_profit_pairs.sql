-- Realized profit: each unit the bot sold paired (FIFO, per SKU) with the earliest earlier unit it
-- bought, valued at the prices the trades used. Rebuilt from trade history by db-manager.
ALTER TABLE trade_events ADD COLUMN realized_profit_refined DOUBLE PRECISION;

CREATE TABLE trade_realized_pairs (
    id                   BIGSERIAL        PRIMARY KEY,
    sku                  VARCHAR(64)      NOT NULL,
    buy_trade_event_id   BIGINT           NOT NULL REFERENCES trade_events(id) ON DELETE CASCADE,
    buy_item_id          BIGINT           NOT NULL REFERENCES trade_event_items(id) ON DELETE CASCADE,
    sell_trade_event_id  BIGINT           NOT NULL REFERENCES trade_events(id) ON DELETE CASCADE,
    sell_item_id         BIGINT           NOT NULL REFERENCES trade_event_items(id) ON DELETE CASCADE,
    cost_refined         DOUBLE PRECISION NOT NULL,
    revenue_refined      DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_trp_buy_event  ON trade_realized_pairs(buy_trade_event_id);
CREATE INDEX idx_trp_sell_event ON trade_realized_pairs(sell_trade_event_id);
