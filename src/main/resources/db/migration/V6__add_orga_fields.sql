ALTER TABLE organisations ADD COLUMN billing_email text NOT NULL DEFAULT '';
ALTER TABLE organisations ADD COLUMN active_from date;
ALTER TABLE organisations ADD COLUMN postal_bills boolean DEFAULT False;