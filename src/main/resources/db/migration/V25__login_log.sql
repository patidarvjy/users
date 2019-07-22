CREATE TABLE login_logs (
    id uuid PRIMARY KEY,
    created timestamp NOT NULL DEFAULT NOW(),
    user_id uuid NOT NULL REFERENCES docutools_users(id)
)