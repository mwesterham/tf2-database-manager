-- Why the bot declined (or sent to review) an offer, e.g. "INVALID_ITEMS: 30540;11 not in pricelist".
-- Null for accepted/cancelled trades and for rows recorded before this column existed.
ALTER TABLE trade_events ADD COLUMN reason TEXT;
