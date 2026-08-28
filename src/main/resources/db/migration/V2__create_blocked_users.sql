CREATE TABLE blocked_users (
    steam_id64  VARCHAR(25)  PRIMARY KEY,
    reason      TEXT,
    blocked_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
