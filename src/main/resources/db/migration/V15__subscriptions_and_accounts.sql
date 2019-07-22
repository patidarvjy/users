ALTER TABLE organisations DROP COLUMN full_available;
ALTER TABLE organisations DROP COLUMN full_used;
ALTER TABLE organisations DROP COLUMN mobile_available;
ALTER TABLE organisations DROP COLUMN mobile_used;
ALTER TABLE organisations DROP COLUMN license_plan;
ALTER TABLE organisations DROP COLUMN payment_type;
ALTER TABLE organisations DROP COLUMN payment_plan;
ALTER TABLE organisations DROP COLUMN postal_bills;

ALTER TABLE docutools_users DROP COLUMN license_type;
ALTER TABLE docutools_users DROP COLUMN licensed_since;
ALTER TABLE docutools_users DROP COLUMN licensed_until;

CREATE TABLE subscriptions (
    id uuid PRIMARY KEY,
    type text NOT NULL,
    organisation_id uuid NOT NULL REFERENCES organisations(id),
    since timestamp NOT NULL DEFAULT NOW(),
    until timestamp,
    payment_type text,
    payment_plan text,
    postal_bills boolean NOT NULL DEFAULT FALSE
);

CREATE TABLE accounts(
    id uuid PRIMARY KEY,
    activated timestamp NOT NULL DEFAULT NOW(),
    user_id uuid REFERENCES docutools_users(id),
    subscription_id uuid NOT NULL REFERENCES subscriptions(id)
);
