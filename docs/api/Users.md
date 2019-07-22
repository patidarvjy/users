# Users API Reference

## 1. Requests in Current User
Base Url: `/api/v2/me`

### 1.1 Current User's Profile

Retrieve the current user's profile information.
#### Request
| Name | Value |
| -------- | ------- |
| URL | `/` |
| Method | `GET` |

#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [User Resource](resources.md#2-user) |
| Success Status Code | `200` |

#### Example Call
`curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/api/v2/me`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 1.2 Update User's Profile
Update the current users profile.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/` |
| Method | `PATCH` |
| Content-Type | `application/json` |
| Resource | [User Resource](resources.md#2-user) |

#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [User Resource](resources.md#2-user) |
| Success Status Code | `200` |

#### Example Call
`curl -X PATCH -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/json" -d '{"firstName": "Tony"}' http://dev.docu.solutions/api/v2/me`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Invalid field values. |
| `401 Unauthorized` | This request requires authentication. |

### 1.3 Change User's Password
Changes the user's password by validating the old password once again and setting a new one.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/password` |
| Method | `PUT` |
| Content-Type | `application/json` |

#### Form Parameters
| Name | Description |
| -------- | ----------------|
| `newPassword` | The users new password. |
| `oldPassword` | The users current password as safety check. |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X PUT -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/x-www-form-urlencoded" -F 'oldPassword=helloworld123' -F 'newPassword=hilo12345678910' http://dev.docu.solutions/api/v2/me/password`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Invalid field values. |
| `401 Unauthorized` | This request requires authentication. |

### 1.4 Change User's Email Address
Initiates email change for the current user. When the passed email address is valid a verification link will be sent to the email address. This address will contain a verification token which then has to be passed to the [Verify new Email Request](#25-verify-new-email-address).

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/email` |
| Method | `PUT` |
| Content-Type | `application/json` |

#### Form Parameters
| Name | Description |
| -------- | --------------- |
| `newEmail` | The user's new email address. |
| `password` | The user's current password to verify the actual user is changing the email address. |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X PUT -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/x-www-form-urlencoded" -F 'password=helloworld123' -F 'email=mynewemail@secret.ninja' http://dev.docu.solutions/api/v2/me/email`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Invalid field values. |
| `401 Unauthorized` | This request requires authentication. |
| `409 Conflict` | The specified email address is already in use. |

### 1.5 Retrieve User's Avatar Thumbnail
Downloads the user's avatar thumbnail image.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/avatar` |
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


#### Example Call
`curl -X GET -H "Authorization: Bearer ${ACCESS_TOKEN} "http://dev.docu.solutions/api/v2/me/avatar`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 1.6 Change User's Avatar
Uploads and replaces the users avatar with a new image. The image must not be larger than 1MB and either content type `image/jpeg`, `image/png`, `image/gif`, `image/svg` or `image/bmp`.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/avatar` |
| Method | `PUT` |
| Content-Type | `multipart/form-data` |

#### Form Data
| Name | Required | Description |
| -------- | ------- | ------- |
| avatar | **yes** | File part with a JPEG, has to be an `image/jpeg` content-type and note more than 128KB. |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X PUT -H "Authorization: Bearer ${ACCESS_TOKEN}" -F "avatar=@/home/user1/Desktop/test.jpg" http://dev.docu.solutions/api/v2/me/avatar`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Either the avatar file was not present, too large (max 256 kilobyte) or neither jpg nor png format. |
| `401 Unauthorized` | This request requires authentication. |

### 1.7 Remove User's Avatar
When the user has an avatar set it will be deleted by this request.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/avatar` |
| Method | `DELETE` |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X DELETE -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/api/v2/me/avatar`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 1.8 Enable Two Factor Authentication
After issuing this request the user will require a one-time-password for each further authentication call. The response of this request is a URL to a QR code, which the user can scan with an OTP app like [Google Authenticator](https://de.wikipedia.org/wiki/Google_Authenticator).

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/2fa` |
| Method | `POST` |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |
| Content-Type | `application/json` |
| Resource | A JSON String containing the URL to the QR code. |

#### Example Call
`curl -X POST -H "Authorization: Bearer ${ACCESS_TOKEN}"http://dev.docu.solutions/api/v2/me/2fa`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 1.9 Disable Two Factor Authentication
Will disable two factor authentication for the current user.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/2fa` |
| Method | `DELETE` |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X DELETE -H "Authorization: Bearer ${ACCESS_TOKEN}"http://dev.docu.solutions/api/v2/me/2fa`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 1.10 Verify New Email Address
Verifies the new Email. If the token is not expired and correct, the change email request will be performed.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/email/verify` |
| Method | `POST` |
| Content-Type | `application/json` |

#### Form Parameters
| Name | Description |
| -------- | ------- |
| token | The verification token. |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X PUT -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/x-www-form-urlencoded" -F 'token=d5e4354b-1ddb-4bcc-a320-45dd904a10d5' http://dev.docu.solutions/api/v2/me/email/verify`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Invalid field values. |
| `401 Unauthorized` | This request requires authentication. |


### 1.11 Generate Two Factor Authentication QR Code
Generates a pairing QR Code for the user, who has enabled the Two Factor Authentication.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/2fa/qr` |
| Method | `GET` |
| Content-Type | `image/png` |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` or `204 No Content` (when two factor authentication is disabled) |

#### Example Call
`curl -X GET -H "Authorization: Bearer ${ACCESS_TOKEN} "http://dev.docu.solutions/api/v2/me/2fa/qr`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 1.12 Get S3 Authorisation Token

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/storage/token` |
| Method | `POST` |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [Cognito Token](resources.md#4-cognito-token) |
| Status Code | `200` **OK** |

#### CURL
`curl -H $AUTHORIZATION_TOKEN http://dev.docu.solutions/api/v2/me/storage/token`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid user id in path. |
| `401` **Unauthorized** | This endpoint requires full authentication. |

### 1.13 Get User Organisation

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/organisation` |
| Method | `GET` |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [Organisation Resource](resources.md#1-organisation) |
| Status Code | `200` **OK** |

#### CURL
`curl -H $AUTHORIZATION_TOKEN http://dev.docu.solutions/api/v2/me/organisation`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |

## Appendix

### Time Zones
The [time-zones.txt](/uploads/367b295580a12ddbeec7520558508519/time-zones.txt) contains a list with all supported time zone IDs for the user preferred time zone settings.

### 1.14 Retrieve User's Original Avatar
Downloads the user's avatar image.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/avatar/original` |
| Method | `GET` |


#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` or `204 No Content` (when no avatar is set) |
| Content-Type | `image/jpeg`, `image/png` |

#### Example Call
`curl -X GET -H "Authorization: Bearer ${ACCESS_TOKEN} "http://dev.docu.solutions/api/v2/me/avatar/original`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |