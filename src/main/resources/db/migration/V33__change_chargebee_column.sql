ALTER TABLE "organisations" DROP COLUMN charge_bee_id;
ALTER TABLE "organisations" ADD COLUMN has_chargebee_account boolean DEFAULT FALSE;
