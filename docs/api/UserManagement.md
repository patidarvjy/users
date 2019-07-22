# User Management API
Allows organisation administrators and project creators to query users in their own organisation and manage them.

1. [Retrieving Users](#1-retrieving-users)
 * [1.1 Paging All Users in Organisation](#11-paging-all-users-in-organisation)
 * [1.2 Invite a New User to your Organisation](#12-invite-a-new-user-to-your-organisation)
 * [1.3 Update a User in the Current User's Organisation](#13-update-a-user-in-the-current-users-organisation)
 * [1.4 Save New Avatar](#14-save-new-avatar)
 * [1.5 Remove Avatar](#15-remove-avatar)
 * [1.6 Get User's Avatar Thumbnail](#16-get-users-avatar-thumbnail)
 * [1.7 Get User by ID](#17-get-user-by-id)
 * [1.8 Download User as VCard](#18-download-user-as-vcard)
 * [1.9 Update Users in Batch](#19-update-users-in-batch)
 * [1.10 Search Users Through All Organisation](#110-search-users-through-all-organisation)
 * [1.11 Get User's Avatar Original](#111-get-users-avatar-original)
 * [1.12 Send Reinvitation Email](#112-send-reinvitation-email)
 * [1.13 Remove User License](#113-remove-user-license)
 * [1.14 Assign User License](#114-assign-user-license)
 
## 1. Retrieving Users

### 1.1 Paging All Users in Organisation

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users` |
| Method | `GET` |
| Required Authorities | `admin`, `project_creator` |

#### Query Parameters
| Name | Required (y/n) | Description |
| ---- | -------------- | ----------- |
| `page` | *no* (Default: `0`) | Page number to return, starting with `0`. |
| `pageSize` | *no* (Default: `10`) | Page size to return, must be greater than `0`. |
| `filter` | *no* (Default: `Any`) | Quick filter for response, allowed values: `Any`, `Licensed`, `WithoutLicense` |
| `sort` | *no* (Default: `name`) | Sort property for response, allowed values: `id`, `name`, `license`, `licensed_since`, `licnsed_until`, `admin`, `projectCreator`, `paid` |
| `sortDirectin` | *no* (Default: `ASC`) | Sorting direction, allowed values: `ASC`, `DESC`. |
| `search` | *no* | Search string for username. |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | Page of [User Profiles](resources.md#2-user) |
| Status Code | `200` **OK** |

#### CURL
`curl -H $AUTHORIZATION_TOKEN http://dev.docu.solutions/api/v2/users`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Possible reasons: Negative page number, page size lower or equal to zero, filter invalid, sort property unknown, sort direction invalid. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin` or `project_creator`. |
| `404` **Not Found** | No user with this ID exists. |

### 1.2 Invite a New User to your Organisation
Send an invitation email to a new user for your docutools organisation.
Note: You can only set `email`, `username`, `firstName`, `lastName`, `type`, `userId`, `language` (in `settings`) properties of the required User resource.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users` |
| Method | `POST` |
| Required Authorities | `admin` |
| Content-Type | `application/json` |
| Resource | [User](resources.md#2-user) (only `email` property is required) |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [User Profile](resources.md#2-user) |
| Status Code | `201` **Created** |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X POST -d '{"email": "helloworld@example.com"}' http://dev.docu.solutions/api/v2/users`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | `email` property in body is `null` or empty. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin`. |
| `404` **Not Found** | No user with this ID exists. |
| `409` **Conflict** | The email address is already in use. |

### 1.3 Update a User in the Current User's Organisation

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}` |
| Method | `PATCH` |
| Required Authorities | `admin` |
| Content-Type | `application/json` |
| Resource | [User](resources.md#2-user) |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [User Profile](resources.md#2-user) |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X PATCH -d '{"firstName": "Tony", "lastName": "Dancer"}' http://dev.docu.solutions/api/v2/users/{userId}`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid user data in the body. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin`, the users have to be in the same organisation, only owners can update themselves or other admins. |
| `404` **Not Found** | No user with this ID exists. |

### 1.4 Save New Avatar

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}/avatar` |
| Method | `PUT` |
| Required Authorities | `admin`, `projectCreator` |
| Content-Type | `multipart/form-data` |

#### Request Parts
| Name | Content-Type |
| ---- | ----- |
| `avatar` | `image/png`, `image/png`, `image/gif`, `image/svg`, `image/bmp` |

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X PUT -F "avatar=@avatar.png" http://dev.docu.solutions/api/v2/users/{userId}/avatar`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid content type. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin`, the users have to be in the same organisation, only owners can update themselves or other admins. |
| `404` **Not Found** | No user with this ID exists. |
| `413` **Payload Too Large** | Image exceeds file size limit of 1MB. |

### 1.5 Remove Avatar

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}/avatar` |
| Method | `DELETE` |
| Required Authorities | `admin`, `projectCreator` |

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X DELETE http://dev.docu.solutions/api/v2/users/{userId}/avatar`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin`, the users have to be in the same organisation, only owners can update themselves or other admins. |
| `404` **Not Found** | No user with this ID exists. |

### 1.6 Get User's Avatar Thumbnail

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}/avatar` |
| Method | `GET` |

### Request Headers
| Name | Value | Description | 
|---------|--------|---------|
|`If-None-Match` | File checksum value | Checksum value will be returned in Response Header `ETag`. If `If-None-Match` matches to the checksum of file then it will return `304` NotModified otherwise return file |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` or `204 No Content` (when no avatar is set). `304` *Not Modified* when `If-None-Match` matches to the checksum of file|
| Content-Type | `image/jpeg`, `image/png` |

### Response Headers
| Name | Value | Description | 
|---------|--------|---------|
|`ETag` | File checksum value | Checksum value of file, can be used to file cache and to send as `If-None-Match` tag for next request for the resource.|

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" http://dev.docu.solutions/api/v2/users/{userId}/avatar`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `404` **Not Found** | No user with this ID exists. |

### 1.7 Get User By Id

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}` |
| Method | `GET` |
| Required Authorities | `admin`, `project_creator` |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [User Profile](resources.md#2-user) |
| Status Code | `200` **OK** |

#### CURL
`curl -H $AUTHORIZATION_TOKEN http://dev.docu.solutions/api/v2/users/{userId}`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid user id in path. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin` or `project_creator`. |
| `404` **Not Found** | No user with this ID exists. |

### 1.8 Download User as VCard

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}/vcard` |
| Method | `GET` |
| Required Authorities | `admin`, `project_creator` |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `text/x-vcard` |
| Status Code | `200` **OK** |

#### CURL
`curl -H $AUTHORIZATION_TOKEN http://dev.docu.solutions/api/v2/users/{userId}/vcard`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid user id in path. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin` or `project_creator`. |
| `404` **Not Found** | No user with this ID exists. |

### 1.9 Update Users in Batch

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users` |
| Method | `PATCH` |
| Required Authorities | `admin`, `project_creator` |
| Resource | [Batch User Update](resources.md#21-userbatch-update) |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Status Code | `200` **OK** |
| Resource | List of [Users](resources.md#2-user) |

#### CURL
`curl -X PATCH -H "Authorization Bearer $AUTHORIZATION_TOKEN" -d '{"ids": [1,2], "update" {"fax": "123456789"}}' http://dev.docu.solutions/api/v2/users`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid user ids in path. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin` or `project_creator`. |
| `404` **Not Found** | No users with this IDs exists. |


### 1.10 Search Users Through All Organisation

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/members` |
| Method | `GET` |
| Required Authorities | `admin`, `project_creator` |

#### Query Parameters
| Name | Required (y/n) | Description |
| ---- | -------------- | ----------- |
| `page` | *no* (Default: `0`) | Page number to return, starting with `0`. |
| `pageSize` | *no* (Default: `10`) | Page size to return, must be greater than `0`. |
| `search` | *no* | Search string for username. |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | Page of [User Profiles](resources.md#2-user) |
| Status Code | `200` **OK** |

#### CURL
`curl -H $AUTHORIZATION_TOKEN http://dev.docu.solutions/api/v2/members`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Possible reasons: Negative page number, page size lower or equal to zero, filter invalid, sort property unknown, sort direction invalid. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin` or `project_creator`. |
| `404` **Not Found** | No user with this ID exists. |

### 1.11 Get User's Avatar Original

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}/avatar/original` |
| Method | `GET` |

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** or `204` **No Content** |
| Content-Type | `image/jpeg`, `image/png` |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" http://dev.docu.solutions/api/v2/users/{userId}/avatar/original`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `404` **Not Found** | No user with this ID exists. |

### 1.12 Send Reinvitation Email

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}/reInvite` |
| Method | `POST` |
| Required Authorities | `admin` |

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X POST " http://dev.docu.solutions/api/v2/users/{userId}/reInvite`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin` |
| `404` **Not Found** | No user with this ID exists. |

### 1.13 Remove User License
Remove the license from the given user.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}/license` |
| Method | `DELETE` |
| Required Authorities | `owner` |

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X DELETE " http://dev.docu.solutions/api/v2/users/{userId}/license`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin` |
| `404` **Not Found** | No user with this ID exists. |

### 1.14 Assign User License
Assign a license to a given user.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/{userId}/license` |
| Method | `POST` |
| Required Authorities | `owner` |

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X POST " http://dev.docu.solutions/api/v2/users/{userId}/license`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin` |
| `404` **Not Found** | No user with this ID exists. |

### 1.15 Unsubscribe from Emails
Unsubscribe from emails.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/unsubscribe` |
| Method | `GET` |

#### Query Parameters
| Name | Required (y/n) | Description |
| ---- | -------------- | ----------- |
| `userId` | yes | The user id that should unsubscribe from emails. |

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X POST " http://dev.docu.solutions/api/v2/users/unsubscribe?userId={userId}`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `404` **Not Found** | No user with this ID exists. |