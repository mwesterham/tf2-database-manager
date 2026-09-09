-- Migrate buy/sell prices from NUMERIC refined to BIGINT half-scrap
-- 1 ref = 9 scrap = 18 half-scrap, so halfScrap = ROUND(metal * 18)

ALTER TABLE pricelist
    RENAME COLUMN buy_metal TO buy_half_scrap;

ALTER TABLE pricelist
    ALTER COLUMN buy_half_scrap TYPE BIGINT USING ROUND(buy_half_scrap * 18);

ALTER TABLE pricelist
    RENAME COLUMN sell_metal TO sell_half_scrap;

ALTER TABLE pricelist
    ALTER COLUMN sell_half_scrap TYPE BIGINT USING ROUND(sell_half_scrap * 18);
