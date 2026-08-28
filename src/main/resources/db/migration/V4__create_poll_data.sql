-- Tracks each trade offer's last-known state for polling continuity.
CREATE TABLE poll_data (
    offer_id            VARCHAR(50)  PRIMARY KEY,
    direction           VARCHAR(10)  NOT NULL CHECK (direction IN ('sent', 'received')),
    state               INTEGER      NOT NULL,
    partner_steam_id64  VARCHAR(25),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_poll_data_direction ON poll_data (direction);
CREATE INDEX idx_poll_data_state     ON poll_data (state);
