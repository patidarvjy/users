ALTER TABLE organisations ADD COLUMN idp_link text;
ALTER TABLE docutools_users ADD COLUMN user_type text default 'Password';