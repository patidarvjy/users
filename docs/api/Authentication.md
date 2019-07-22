# docutools Authorization Mechanics
This pages describes how authentication and authorization mechanics for docutools work in detail. It is intended to be read by backend as well as frontend developers.

## 1. OAuth2 + JSON Web Tokens
OAuth2 is the industry standard for authentication ([spec here](https://oauth.net/2/)). Since we use the Spring Framework Version 4 as our main Backend Technology we employ the [Spring Cloud Security OAuth2](https://projects.spring.io/spring-security-oauth/docs/oauth2.html) implementation to implement OAuth2 as authentication mechanism together with [Spring Security JWT](http://www.baeldung.com/spring-security-oauth-jwt). JWT stands for JSON Web Tokens and are a handy transportation mechanism of an authentication grant for distributed systems.

To get a better understanding of OAuth2 and JWT read [Digital Ocean's Introduction to OAuth 2](https://www.digitalocean.com/community/tutorials/an-introduction-to-oauth-2) and browse through [jwt.io's Introduction page](https://jwt.io/introduction/).

With a basic understanding of OAuth 2 you know there are the concepts of **Authorization** and **Resource** servers. The **Resource** server accept JWTs granted by the **Authorization** servers they trust. The following graphic explains how this roles are assigned in docutools:

![docutools_oauth_diagram](/uploads/47d6301e666466b6f35783e6da0517d4/docutools_oauth_diagram.png)

1. Clients authenticate users against this users service.
2. The users service returns a signed JWT which is valid for a specific time.
3. Clients can request resources from any other resource server in the system by specifying the JWT's access token in the Authorization HTTP header.
4. Clients can refresh the access token using the JWTs refresh token.

### Resource Servers (Backend only)
To configure your service to require authentication refer to [this wiki page](resource-server-config).

### Granted Authorities (aka Owner, Admin and Project Creator)
Users can have four different roles in their organisations:

1. **Organisation owners:** The user initially created the organisation and is allowed to do everything, including granting other users in the organisation admin status as well as revoking it.
2. **Organisation admins:** They can nearly do everything in an organisation, except granting and revoking admin status to other users.
3. **Project creators:** They can create, update and archive projects and folders as well as invite any user to a project they created automatically and manage project roles in the organisation.
4. **Normal users:** Don't have any special rights, only the ones assigned to them by project roles.

These roles are reflected in two places:
1. For the backend they are set as Spring Security granted authorities can be queries through [Expression-Based Access Control](http://docs.spring.io/spring-security/site/docs/3.0.x/reference/el-access.html).
2. If a user is admin or project creator can be queries in the profile settings when retrieving a user profile (for the frontend).

## 2. Two Factor Authentication
*Coming soon.*