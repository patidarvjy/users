ALTER TABLE project_contacts ADD COLUMN replaced BOOLEAN NOT NULL DEFAULT False;
ALTER TABLE project_contacts ADD COLUMN replaced_by_id uuid REFERENCES docutools_users(id);