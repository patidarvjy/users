CREATE TABLE login_counts (
    id UUID PRIMARY KEY REFERENCES docutools_users(id),
    total int NOT NULL DEFAULT 0
);