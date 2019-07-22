ALTER TABLE "docutools_users" ADD COLUMN "invited_by" uuid REFERENCES "docutools_users"("id");
