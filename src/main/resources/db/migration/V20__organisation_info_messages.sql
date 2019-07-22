CREATE TABLE no_license_messages (
 org_id uuid NOT NULL REFERENCES organisations(id),
 lang text NOT NULL,
 message text NOT NULL,
 CONSTRAINT no_license_messages_pk PRIMARY KEY (org_id, lang)
);