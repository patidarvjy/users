CREATE TABLE project_contacts (
    id uuid NOT NULL PRIMARY KEY,
    project_id uuid NOT NULL,
    company_name text NOT NULL DEFAULT '',
    email text DEFAULT '',
    first_name text NOT NULL DEFAULT '',
    last_name text NOT NULL DEFAULT '',
    phone varchar(32) DEFAULT '',
    fax text DEFAULT '',
    job_title text DEFAULT '',
    comment text NOT NULL DEFAULT '',
    department text DEFAULT '',
    internal_id text DEFAULT '' ,
    street text DEFAULT '',
    zip varchar(12) DEFAULT '',
    city text DEFAULT '',
    created timestamp NOT NULL DEFAULT NOW(),
    last_modified timestamp NOT NULL DEFAULT NOW()
 );