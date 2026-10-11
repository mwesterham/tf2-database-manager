CREATE TABLE trade_event_bptf_listings (
    id              BIGSERIAL   PRIMARY KEY,
    trade_event_id  BIGINT      NOT NULL REFERENCES trade_events(id) ON DELETE CASCADE,
    sku             VARCHAR(64) NOT NULL,
    intent          VARCHAR(4)  NOT NULL,
    keys            INT         NOT NULL DEFAULT 0,
    half_scrap      BIGINT      NOT NULL DEFAULT 0,
    stock           INT         NOT NULL DEFAULT 0,
    listing_limit   INT         NOT NULL DEFAULT 0
);
CREATE INDEX idx_tebl_event_id ON trade_event_bptf_listings(trade_event_id);
