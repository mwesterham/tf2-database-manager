ALTER TABLE pricelist ADD COLUMN IF NOT EXISTS integ_test_only BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_pricelist_integ_test_only ON pricelist (integ_test_only) WHERE integ_test_only = TRUE;
