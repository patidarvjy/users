CREATE TABLE password_log (
    user_id uuid NOT NULL REFERENCES docutools_users(id),
    "password" text NOT NULL
);

CREATE INDEX password_log_user_id_index ON  password_log(user_id);

ALTER TABLE organisations ADD COLUMN password_policy TEXT;