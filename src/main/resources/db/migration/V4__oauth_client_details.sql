create table oauth_client_details (
  client_id VARCHAR(256) PRIMARY KEY,
  resource_ids VARCHAR(256),
  client_secret VARCHAR(256),
  scope VARCHAR(256),
  authorized_grant_types VARCHAR(256),
  web_server_redirect_uri VARCHAR(256),
  authorities VARCHAR(256),
  access_token_validity INTEGER,
  refresh_token_validity INTEGER,
  additional_information VARCHAR(4096),
  autoapprove VARCHAR(256)
);

INSERT INTO oauth_client_details (client_id,client_secret,scope,authorized_grant_types)
VALUES ('android','L%E2pvnQ','users,organisations','password,refresh_token'),
       ('ios','vzh8Vc$E','users,organisations','password,refresh_token'),
       ('web','=z~;5jWd','users,organisations','password,refresh_token'),
       ('tester','secret','users,organisations','password,refresh_token');
