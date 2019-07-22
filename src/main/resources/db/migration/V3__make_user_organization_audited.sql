ALTER TABLE "organisations"
     ADD COLUMN created timestamp ,
     ADD COLUMN last_modified timestamp;

ALTER TABLE "docutools_users"
     ADD COLUMN created timestamp ,
     ADD COLUMN last_modified timestamp;