# Memberships API
This document contains the *Reference Documentation* for the API Endpoints creating, retrieving and updating memberships of users in projects.

Each membership can be understood as a many to many relationship between projects and user.

Memberships can have many roles which define the privileges a user has in the project.

A Membership can have one of three states:

1. **Inactive**: The user is not member of of the team and the assigned roles have no effect.
2. **Invited**: The user got invited to the team (when not a docutools user then per email), but has not accepted yet.
3. **Active**: Team member.

###Index
1. [Team Member Resource](#1-team-member-resource)
2. [Get User Project Matrix](#2-get-user-project-matrix)
3. [Get Project's Team](#3-get-projects-team)
4. [Add New Member](#4-add-a-new-member)
5. [Update a Member](#5-update-a-member)
6. [Search Team Member](#6-search-team-member)
7. [Update Users Memberships in Bulk](#7-update-users-memberships-in-bulk)
8. [Remove Membership](#8-remove-membership)
9. [Add Current User To Team](#9-add-current-user-to-team)
10. [Get Project Role](#10-get-project-role]
11. [Invite Members in Bulk](#11-invite-members-in-bulk)

## 1. Team Member Resource

```json
{
  "userId": {
    "type": "uuid",
    "nullable": false,
    "readonly": true,
    "description": "The unique ID of the user."
  },
  "name": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "description": "Full name of the user."
  },
  "companyId": {
    "type": "uuid",
    "nullable": false,
    "readonly": true,
    "description": "ID of the user's company/organisation."
  },
  "companyName": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "description": "Name of the user's company."
  },
  "email": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "description": "The user's email address."
  },
  "phone": {
    "type": "String",
    "nullable": true,
    "readonly": true,
    "description": "The user's phone number."
  },
  "license": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "values": ["None", "MobileOnly", "Full"],
    "description": "The user's license."
  },
  "licenseUntil": {
    "type": "Instant",
    "nullable": false,
    "readonly": true,
    "description": "The license enddate."
  },
  "projectId": {
    "type": "uuid",
    "nullable": false,
    "readonly": true,
    "description": "The ID of the project the user is member of."
  },
  "projectName": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "description": "The name of the project the user is member of."
  },
  "department": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "description": "The department of the user."
  },
  "street": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "description": "The street of the user."
  },
  "zip": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "description": "The zip code of the city of the user."
  },
  "city": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "description": "The city of the user."
  },
  "state": {
    "type": "String (Enum)",
    "nullable": false,
    "values": ["Inactive", "Invited", "Active", "Removed"],
    "description": "The user's membership state in this project."
  },
  "invitedSince": {
    "type": "String (Timestamp)",
    "nullable": true,
    "description": "When the user was invited to the project."
  },
  "roles": {
    "type": "List of Roles",
    "nullable": false,
    "readonly": true,
    "description": "The roles the user got assigned in this project, can be empty. Deprecated."
  },
  "roleIds": {
    "type": "List of Role UUIDs",
    "nullable": true,
    "description": "Used for updating the roles the user has in this project."
  },
  "role": {
    "type": "Role",
    "nullable": false,
    "description": "The role the user got assigned in this project."
  },
  "permissions": {
    "type": "List of Strings",
    "readonly": true,
    "values": "Edit",
    "description": "The permissions the teammember has."
  },
  "licenseInfo": {
    "type": "User Account",
    "nullable": true,
    "readonly": true,
    "description": "The user account with the licenseinfo of the user.",
  },
  "account": {
    "type": "User Account",
    "nullable": true,
    "readonly": true,
    "description": "The user account with the licenseinfo of the user.",
  }
}
```

## 2. Get User Project Matrix
This endpoint returns a list of all projects of the current user's organisation and which roles the specified user has in this project.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}/projects` |
| Method | `GET` |
| Required Authorities | *none* (when same user) or *projectCreator*, *admin* |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | Array of [Memberships](#1-team-member-resource) |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Authorization $AUTHORIZATION_TOKEN" http://dev.docu.solutions/api/v2/users/{userId}/projects`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has not the required authorities to list this. |
| `404` **Not Found** | No user with this ID found. |

## 3. Get Project's Team
Lists all [Team Member](#1-team-member-resource) in a project.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/projects/{projectId}/team` |
| Method | `GET` |
| Required Authorities | *projectCreator*, *admin* in the project's organisation or privileges `ViewTeam` or `Manage Team` |

#### Query Params
| Name | Required (y/n) | Description |
| ---- | -------------- | ----------- |
| `quickFilter` | **no** (Default: `All`) | Filters team members by their company, either `All`, `MyCompany` or `OtherCompanies` |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | Array of [Team Members](#1-team-member-resource) |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Authorization $AUTHORIZATION_TOKEN" http://dev.docu.solutions/api/v2/projects/{projectId}/team`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has not the required authorities to list this. |
| `404` **Not Found** | No project with this ID found. |

## 4. Add New Member
Adds a new member to a project.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/projects/{projectId}/team` |
| Method | `POST` |
| Required Authorities | *projectCreator*, *admin* in the project's organisation or privilege `Manage Team` |
| Request Body | [Team Member](#1-team-member-resource) |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [Team Member](#1-team-member-resource) |
| Status Code | `201` **Created** or `200` **OK** when already existed. |

#### CURL
`curl -H "Authorization $AUTHORIZATION_TOKEN" -X POST -d '{"userId": 1, "roleIds": ["6d7318c1-e796-49e4-b498-4658afa55503"]}' http://dev.docu.solutions/api/v2/projects/{projectId}/team`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid project ID, request body, missing roles or userID. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has not the required authorities to list this. |
| `404` **Not Found** | No project or user with this ID found. |

## 5. Update a Member
Updates a members state or roles.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/projects/{projectId}/team/{userId}` |
| Method | `PATCH` |
| Required Authorities | *projectCreator*, *admin* in the project's organisation or privilege `Manage Team` |
| Request Body | [Team Member](#1-team-member-resource) |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [Team Member](#1-team-member-resource) |
| Status Code | `200` **OK** or `201` **Created** (When User was not a member before) |

#### CURL
`curl -H "Authorization $AUTHORIZATION_TOKEN" -X PATCH -d '{"state": "Active", "roleIds": ["6d7318c1-e796-49e4-b498-4658afa55503"]}' http://dev.docu.solutions/api/v2/projects/{projectId}/team/{userId}`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid project ID, request body, missing roles or userID. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has not the required authorities to list this. |
| `404` **Not Found** | No project or user with this ID found. |

## 6. Search Team Member
End point to search Team member in project
#### Request
|Name|Value|
|--------|-------|
|URL|`/api/v2/team/search?search="Text to search"&for={projectId}`|
|Method|`GET`|

#### Request Parameters
|Name|Description|Required|
|--------|---------------|------------|
|`search`|Text to search(Search on Projects names)|`Required`|
|`for`|project id|`Required`|

#### Response
|Name|Value|
|--------|-------|
|Success Status Code|`200`|
|Response Type| List of [Team Member](#1-team-member-resource)|

#### Example  
`curl -X GET  -H  http://dev.docu.solutions/api/v2/team/search?search="name"&for={projectId}`

#### Errors
|Status Code|Reasons|
|----------------|-----------|
|`403 Forbidden`|If user does not have permissions to perform this operation.|
|`401 Not Authorised`|If request is not properly authorised.|


## 7. Update Users Memberships in Bulk
Add or remove roles for a user in different projects.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}/memberships` |
| Method | `PATCH` |
| Required Authorities | *projectCreator*, *admin* in the project's organisation or privilege `Manage Team` in all the projects. |
| Request Body | Object mapping project IDs to list of role IDs. |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Authorization $AUTHORIZATION_TOKEN" -X PATCH -d '{"1": ["6d7318c1-e796-49e4-b498-4658afa55503"]}' http://dev.docu.solutions/api/v2/users/{userId}/memberships`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid project IDs, request body, missing roles or userID. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has not the required authorities to list this. |
| `404` **Not Found** | No user or user with this ID found. |

## 8. Remove Membership
Removes a Member form a Project Team.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/projects/{projectId}/team/{userId}` |
| Method | `DELETE` |
| Required Authorities | *projectCreator*, *admin* in the project's organisation or privilege `Manage Team` in the project. |

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Authorization $AUTHORIZATION_TOKEN" -X DELETE  https://dev.docu.solutions/api/v2/projects/{projectId}/team/{userId}`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid project or user ID. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has not the required authorities to list this. |
| `404` **Not Found** | No user or project with this ID found. |

## 9. Add Current User To Team
Adds the current user to the team of the created project.
This endpoint is for project creators, who just created a project, so they can add themself to the team.
It will add the standard role (Currently Power User) to the project creator.
**Note**: This endpoint is currently only in use in the backend.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/projects/{projectId}/team/creator` |
| Method | `PUT` |
| Required Authorities | *projectCreator*, *admin* in the project's organisation. |

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Authorization $AUTHORIZATION_TOKEN" -X PUT https://dev.docu.solutions/api/v2/projects/{projectId}/team/creator`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid project or user ID. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has not the required authorities to list this. |
| `404` **Not Found** | No user or project with this ID found. |

## 11. Invite Members in Bulk
Invites new team members (either existing users or new users by email) to the project.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/projects/{projectId}/team/many` |
| Method | `POST` |
| Required Authorities | *projectCreator*, *admin* in the project's organisation or privilege `Manage Team` in the project. |
| Request Body | [Team Member Bulk](resource.md#11-invite-members-in-bulk-resource) |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Authorization $AUTHORIZATION_TOKEN" -X POST -d '{"emails": ["john@example.com"], "active": true, "roleId": "49f42116-be1c-4330-ac66-7f55e27e68e1
"}' http://dev.docu.solutions/api/v2/projects/{projectId}/team/many`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid project ID, request body, invalid role ID. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has not the required authorities to list this. |
| `404` **Not Found** | No user or project with this ID found. |

## 12. Get User Role for Project
Returns the Role for the current user of the given project.

#### Request
|Name|Value|
|--------|-------|
|URL|`/api/v2/me/role`|
|Method|`GET`|

#### Request Parameters
|Name|Description|Required|
|--------|---------------|------------|
|`projectId`|project id|`Required`|

#### Response
|Name|Value|
|--------|-------|
|Success Status Code|`200`|
|Response Type| [User Role](resource.md#12-user-role)|

#### Example
`curl -X GET -H "Authorization $AUTHORIZATION_TOKEN" http://dev.docu.solutions/api/v2/me/role?projectId={projectId}`

#### Errors
|Status Code|Reasons|
|----------------|-----------|
|`401 Not Authorised`|If request is not properly authorised.|

## 13. Copy Project Members
Copies the project members of one project to another.

#### Request
|Name|Value|
|--------|-------|
|URL|`/api/v2/projects/{projectId}/team`|
|Method|`PUT`|

#### Request Parameters
|Name|Description|Required|
|--------|---------------|------------|
|`from`|Id of the project to be copied from|`Required`|

#### Response
|Name|Value|
|--------|-------|
|Success Status Code|`200`|
|Response Type| [User Role](resource.md#12-user-role)|

#### Example
`curl -X GET -H "Authorization $AUTHORIZATION_TOKEN" http://dev.docu.solutions/api/v2/me/role?projectId={projectId}`

#### Errors
|Status Code|Reasons|
|----------------|-----------|
|`401 Not Authorised`|If request is not properly authorised.|