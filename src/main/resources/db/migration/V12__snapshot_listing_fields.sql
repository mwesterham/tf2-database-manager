ALTER TABLE trade_event_snapshots
    ADD COLUMN enabled      BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN autoprice    BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN min_stock    INT     NOT NULL DEFAULT 0,
    ADD COLUMN max_stock    INT     NOT NULL DEFAULT 1,
    ADD COLUMN auto_managed BOOLEAN NOT NULL DEFAULT false;
