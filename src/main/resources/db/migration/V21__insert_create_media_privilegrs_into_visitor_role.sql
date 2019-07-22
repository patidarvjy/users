--Privileges are saved as ordinal values in db so 21 is for CreateMedia privilege
INSERT INTO role_privileges (privileges,role_id) (SELECT 21, id FROM roles WHERE role_type='Viewer');