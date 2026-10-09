ALTER TABLE trade_events
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'accepted';
