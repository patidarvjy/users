CREATE TABLE organisation_names (
    id uuid PRIMARY KEY,
    name text NOT NULL,
    organisation_id uuid NOT NULL REFERENCES organisations(id),
    CONSTRAINT name_in_org_unique UNIQUE(name, organisation_id)
);

ALTER TABLE docutools_users ADD COLUMN organisation_name_id uuid REFERENCES organisation_names(id);