CREATE TABLE "organisations" (
    "id" uuid PRIMARY KEY,
    "name" text,
    "vat_number" text,
    "vat_valid" boolean,
    "cc" text,
    license_plan TEXT NOT NULL DEFAULT 'None',
    payment_plan TEXT,
    full_available INT NOT NULL DEFAULT 0,
    full_used INT NOT NULL DEFAULT 0,
    mobile_available INT NOT NULL DEFAULT 0,
    mobile_used INT NOT NULL DEFAULT 0,
    CONSTRAINT check_full_licenses CHECK(full_available - full_used >= 0),
    CONSTRAINT check_mobile_licenses CHECK(mobile_available - mobile_used >= 0)
);

CREATE TABLE "docutools_users" (
    "id" uuid NOT NULL PRIMARY KEY,
    "organisation_id" uuid NOT NULL REFERENCES "organisations"("id"),
    "username" varchar(255) NOT NULL UNIQUE,
    "first_name" varchar(128) NOT NULL DEFAULT '',
    "last_name" varchar(128) NOT NULL DEFAULT '',
    "phone" varchar(32) DEFAULT '',
    "job_title" varchar(128) DEFAULT '',
    "comment" text NOT NULL DEFAULT '',
    "time_zone" varchar(255),
    "language" char(2) NOT NULL DEFAULT 'en',
    "two_factor_auth_enabled" boolean NOT NULL DEFAULT FALSE,
    "two_fa_secret" varchar(255),
    "admin" boolean NOT NULL DEFAULT FALSE,
    "project_creator" boolean NOT NULL DEFAULT FALSE,
    "active" boolean NOT NULL DEFAULT TRUE,
    "password_hash" varchar(255),
    "password_last_changed" timestamp,
    "password_hash_version" varchar(255),
    verified boolean NOT NULL DEFAULT TRUE,
    "verification_required" boolean NOT NULL DEFAULT TRUE,
    "verification_token" varchar(255),
    "verification_token_expiry_time" timestamp,
    license_type TEXT NOT NULL DEFAULT 'None',
    licensed_since TIMESTAMP,
    licensed_until TIMESTAMP,
    fax varchar(128),
    department varchar(128),
    internal_id varchar(128),
    email varchar(128),
    street varchar(128),
    zip varchar(12),
    city varchar(128)
);

-- add and set owner_id in organisations
ALTER TABLE "organisations" ADD COLUMN "owner_id" uuid REFERENCES "docutools_users"("id");
UPDATE "organisations" SET "owner_id" = "id";

CREATE TABLE "profile_pic" (
    "owner_id" uuid PRIMARY KEY REFERENCES "docutools_users"("id"),
    "content_type" text NOT NULL,
    "data" bytea NOT NULL
);

CREATE TABLE "change_email_requests" (
    "id" uuid PRIMARY KEY,
    "user_id" uuid REFERENCES "docutools_users"("id"),
    "new_email" TEXT NOT NULL,
    "timestamp" TIMESTAMP NOT NULL,
    "verification_token" TEXT NOT NULL,
    "verified" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE roles (
    id uuid PRIMARY KEY NOT NULL,
    name varchar(64) NOT NULL,
    organisation_id uuid NOT NULL REFERENCES organisations(id),
    created_by_id uuid NOT NULL,
    last_modified timestamp NOT NULL,
    active boolean NOT NULL DEFAULT True
);

CREATE TABLE role_privileges (
    role_id uuid NOT NULL,
    privileges text NOT NULL
);

CREATE TABLE team_memberships (
    id uuid PRIMARY KEY NOT NULL,
    user_id uuid NOT NULL REFERENCES docutools_users(id),
    project_id uuid NOT NULL,
    invited timestamp NOT NULL,
    state text NOT NULL,
    CONSTRAINT user_once_per_project UNIQUE(user_id, project_id)
);

CREATE TABLE role_assignments (
    member_id uuid NOT NULL REFERENCES team_memberships(id),
    role_id uuid NOT NULL REFERENCES roles(id)
);