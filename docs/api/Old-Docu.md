# Users Service - REST API Reference
Reference documentation for the REST API.

**Here the Postman Collection and an Example Environment for testing:**

[docutools_users_postman_env.json](/uploads/f205cd9482ef16b6970853bfd6c844f1/docutools_users_postman_env.json)

*New Version:* [In the repository root folder.](https://gitlab.docu.solutions/docutools/users-service/blob/staging/postman_collection.json)

*Old Version:* [docutools-users_postman.json](/uploads/80c114a387117f3103584585f4af24ed/docutools-users_postman.json) (11th of April 2017 by Alexander Partsch)

1. [Resources](#1-resources)
  * [1.1 Registration Resource](#11-registration-resource)
  * [1.2 Verification Resource](#12-verification-resource)
  * [1.3 User Resource](#13-user-resource)
  * [1.4 Organisation Resource](#14-organisation-resource)
  * [1.5 Contact Resource](#15-contact-resource)
  * [1.6 Address Resource](#16-address-resource)
  * [1.7 AWS Cognito Token Resource](#17-aws-cognito-token-resource)
2. [Personal API](#2-personal-api) 👷
  * [2.1 Show User's Profile](#21-show-users-profile)
  * [2.2 Show User's Organisation](#22-show-users-organisation)
  * [2.3 Update User's Profile](#23-update-users-profile)
  * [2.4 Change User's Email Address](#24-change-users-email-address)
  * [2.5 Verify New Email Address](#25-verify-new-email-address)
  * [2.6 Change User's Password](#26-change-users-password)
  * [2.7 Enable Two Factor Authentication](#27-enable-two-factor-authentication)
  * [2.8 Disable Two Factor Authentication](#28-disable-two-factor-authentication)
  * [2.9 Retrieve User's Avatar](#29-retrieve-users-avatar)
  * [2.10 Change User's Avatar](#210-change-users-avatar)
  * [2.11 Remove User's Avatar](#211-remove-users-avatar)
  * [2.12 Get QR Code for User's OTP](#212-get-qr-code-for-users-otp)
  * [2.13 Get S3 Authorisation Token](#213-get-s3-authorisation-token)
3. [Registration API](#3-registration-api) 🚪
  * [3.1 Checking if Email Address is Available](#31-checking-if-email-address-is-available)
  * [3.2 Register a new User](#32-register-a-new-user)
  * [3.3 Verify new User](#33-verify-new-user)
4. [Authentication API](#4-authentication-api) 🔑
  * [4.1 Check if Two Factor Authentication is Required](#41-check-if-two-factor-authentication-is-required)
  * [4.2 Authenticate a User](#42-authenticate-a-user)
  * [4.3 Refresh a Token](#43-refresh-a-token)
  * [4.4 Reset Password](#44-reset-password)

## 1. Resources
Description of all resources used in this API.

### 1.1 Registration Resource
Used in registering new users.

```json
{
  "email": {
    "type": "String",
    "nullable": false,
    "constraints": "Non empty, max 128 characters, must contain '@' symbol.",
    "description": "The new user's email address."
  },
  "firstName": {
    "type": "String",
    "nullable": true,
    "constraints": "Max 128 characters.",
    "description": "The user's first name."
  },
  "lastName": {
    "type": "String",
    "nullable": true,
    "constraints": "Max 128 characters.",
    "description": "The user's last name."
  },
  "organisationName": {
    "type": "String",
    "nullable": false,
    "constraints": "Non empty, max 128 characrters.",
    "description": "Name of the users company/organisation."
  },
  "contryCode": {
    "type": "String",
    "nullable": false,
    "constraints": "Two letters.",
    "description": "Two letter ISO 3166-1 alpha-2 country code of the users company/organisation."
  },
  "language": {
    "type": "String",
    "nullable": true,
    "defaultValue": "EN",
    "constraints": "Two letters.",
    "description": "ISO 639-1 code language code for the users preferred language."
  }
}
```

### 1.2 Verification Resource
Used for verifying a registered user with a new password and the verification token.

```json
{
  "token": {
    "type": "String",
    "nullable": false,
    "description": "The verification token sent to the user by email."
  },
  "password": {
    "type": "String",
    "nullable": false,
    "constraints": "At least 12 characters.",
    "description": "The users new password."
  }
}
```

### 1.3 User Resource
Represents a specific user with all his settings and profile information.

```json
{
  "userId": {
    "type": "Number",
    "nullable": false,
    "readonly": true,
    "description": "Unique ID of this user."
  },
  "organisationId": {
    "type": "Number",
    "nullable": false,
    "readonly": true,
    "description": "Reference ID to the users organisation."
  },
  "organisationName": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "description": "Name of the organisation."
  },
  "username": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "constraints": "Non empty, max 128 characters, must contain '@' symbol.",
    "description": "The users email address."
  },
  "firstName": {
    "type": "String",
    "nullable": false,
    "constraints": "Max 128 characters.",
    "description": "The user's first name."
  },
  "lastName": {
    "type": "String",
    "nullable": false,
    "constraints": "Max 128 characters.",
    "description": "The user's last name."
  },
  "phone": {
    "type": "String",
    "nullable": true,
    "constraints": "Max 32 characters.",
    "description": "The user's telephone number."
  },
  "jobTitle": {
    "type": "String",
    "nullable": true,
    "constraints": "Max 128 characters.",
    "description": "The user's job title/description."
  },
  "comment": {
    "type": "String",
    "nullable": true,
    "readonly": true,
    "description": "Legacy field from docutools 2.1, can be ignored from now."
  },
  "settings": {
    "language": {
      "type": "String",
      "nullable": false,
      "constraints": "Two letters.",
      "description": "ISO 639-1 code language code for the users preferred language."
    },
    "timeZone": {
      "type": "String",
      "nullable": true,
      "constraints": "Time zone format [Continent/Region] as listed here: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones.",
      "description": "The users prefered timezone."
    },
    "twoFactorAuthEnabled": {
      "type": "Boolean",
      "nullable": false,
      "description": "Whether the user has two factor authentication enabled or not."
    },
    "admin": {
      "type": "Boolean",
      "nullable": false,
      "readonly": true,
      "description": "When true: indicates that the user is an organisation administrator and has special permissions."
    },
    "projectCreator": {
      "type": "Boolean",
      "nullable": false,
      "readonly": true,
      "description": "When true: indicates the user is project creator and has special permissions."
    }
  },
  "active": {
    "type": "Boolean",
    "nullable": false,
    "readonly": true,
    "description": "Whether the user can login or not. For organisation admins to lock out users."
  }
}
```

### 1.4 Organisation Resource
Represents a company/organisation containing one or many users.

```json
{
  "id": {
    "type": "Number",
    "nullable": false,
    "readonly": true,
    "description": "Unique ID of the organisation."
  },
  "name": {
    "type": "String",
    "nullable": false,
    "constraints": "Non empty, max 128 characters.",
    "description": "User defined name of this organisation."
  },
  "vat": {
    "number": {
      "type": "String",
      "nullable": true,
      "constraints": "Max 32 characters.",
      "description": "The organisations VAT number."
    },
    "valid": {
      "type": "Boolean",
      "nullable": true,
      "readonly": true,
      "description": "If the organisation is in the EU the vat number will be validated (not yet implemented)."
    }
  },
  "cc": {
    "type": "String",
    "nullable": false,
    "constraints": "Two letters.",
    "description": "Two letter ISO 3166-1 alpha-2 country code of the users company/organisation."
  },
  "contacts": {
    "type": "Array of Contact Resources",
    "nullable": false,
    "description": "Contact information associated with this organisation."
  },
  "addresses": {
    "type": "Array of Address Resources",
    "nullable": false,
    "description": "Organisation's addresses."
  }
}
```

### 1.5 Contact Resource
Contact information associated with an organisation.

```json
{
  "id": {
    "type": "Number",
    "nullable": false,
    "readonly": true,
    "description": "Unique ID of this contact."
  },
  "name": {
    "type": "String",
    "nullable": false,
    "constraints": "Non empty, max 128 characters.",
    "description": "Person/company or department name of this contact."
  },
  "email": {
    "type": "String",
    "nullable": false,
    "constraints": "Non empty, max 128 characters.",
    "description": "Email address of the contact."
  },
  "type": {
    "type": "String, [IT|SALES]",
    "nullable": false,
    "description": "Type/classification of this contact."
  }
}
```

### 1.6 Address Resource
Physical address associated with an organisation.

```json
{
  "id": {
    "type": "Number",
    "nullable": false,
    "readonly": true,
    "description": "Unique ID of this address."
  },
  "streetLine": {
    "type": "String",
    "nullable": true,
    "constraints": "Max 128 characters.",
    "description": "Street name and numbers of the contact.",
    "example": "Wall Street 256/32/4, NYC"
  },
  "zipCode": {
    "type": "String",
    "nullable": true,
    "constraints": "Max 20 characters.",
    "description": "Zip code of this address.",
    "example": "00546"
  },
  "cc": {
    "type": "String",
    "nullable": true,
    "constraints": "Two letters.",
    "description": "Two letter ISO 3166-1 alpha-2 country code of this address.",
    "example": "US"
  },
  "homepage": {
    "type": "String",
    "nullable": true,
    "constraints": "Max 128 characters.",
    "description": "Homepage for this address.",
    "example": "https://test.at"
  },
  "type": {
    "type": "String, [HQ|BRANCH|PLANT]",
    "nullable": true,
    "descirption": "Type/classification of this address."
  }
}
```

### 1.7 AWS Cognito Token Resource
Token to be used to access files form s3 bucket/url

```json
{
   "identityId" : {
      "type": "String",
      "readonly": true,
      "nullable" : false,
      "description" : "S3 cognito identity id "
   },
   "token":{
      "type": "String",
      "readonly": true,
      "nullable" : false,
      "description" : "Short lived token for accession S3"
   }
}
```

## 2. Personal API
The personal API contains all requests regarding the current user: retrieving and updating profile information, changing email/password and setting the avatar.

**Base-URL:** `/api/v1/me`

*All requests in this chapter start with this base url!*

### 2.1 Show User's Profile
Get the current user's profile information.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/` |
| Method | `GET` ⬇️ |

#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [User Resource](#13-user-resource) |
| Success Status Code | `200 |

#### Example Call
`curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/api/v1/me`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 2.2 Show User's Organisation
Get the current user's organisation.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/organisation` |
| Method | `GET` ⬇️ |

#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [Organisation Resource](#14-organisation-resource) |
| Success Status Code | `200 |

#### Example Call
`curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/api/v1/me/organisation`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 2.3 Update User's Profile
Update the current users profile.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/` |
| Method | `PATCH` |
| Content-Type | `application/json` |
| Resource | [User Resource](#13-user-resource) |

#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [User Resource](#13-user-resource) |
| Success Status Code | `200` |

#### Example Call
`curl -X PATCH -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/json" -d '{"firstName": "Tony"}' http://dev.docu.solutions/api/v1/me`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Invalid field values. |
| `401 Unauthorized` | This request requires authentication. |

### 2.4 Change User's Email Address
Initiates email change for the current user. When the passed email address is valid a verification link will be sent to the email address. This address will contain a verification token which then has to be passed to the [Verify new Email Request](#25-verify-new-email-address).

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/email` |
| Method | `PUT` |
| Content-Type | `application/json` |

#### Request Body
```json
{
    "password": "Password of the current user, to verify his identity when changing email address.",
   "newEmail": "New email address for the current user."
}
```

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X PUT -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/x-www-form-urlencoded" -F 'password=helloworld123' -F 'email=mynewemail@secret.ninja' http://dev.docu.solutions/api/v1/me/email`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Invalid field values. |
| `401 Unauthorized` | This request requires authentication. |
| `409 Conflict` | The specified email address is already in use. |

### 2.5 Verify New Email Address
After initiating a email change request by submitting a new email address to [this request](#24-change-users-email-address) the user can verify her new email by submitting the sent verification token to this request.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/email/verify` |
| Method | `POST` |
| Content-Type | `application/json` |

#### Request Body
```json
{
    "token": "Email verification token."
}
```

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X PUT -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/x-www-form-urlencoded" -F 'token=d5e4354b-1ddb-4bcc-a320-45dd904a10d5' http://dev.docu.solutions/api/v1/me/email/verify`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Invalid field values. |
| `401 Unauthorized` | This request requires authentication. |

### 2.6 Change User's Password
Changes the user's password by validating the old password once again and setting a new one.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/password` |
| Method | `PUT` |
| Content-Type | `application/json` |

#### Request Body
```json
{
    "oldPassword": "Password of the current user, to verify his identity when changing password.",
   "newPassword": "New password the user wants to set."
}
```

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X PUT -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/x-www-form-urlencoded" -F 'oldPassword=helloworld123' -F 'newPassword=hilo12345678910' http://dev.docu.solutions/api/v1/me/password`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Invalid field values. |
| `401 Unauthorized` | This request requires authentication. |

### 2.7 Enable Two Factor Authentication
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
`curl -X POST -H "Authorization: Bearer ${ACCESS_TOKEN}"http://dev.docu.solutions/api/v1/me/2fa`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |


### 2.8 Disable Two Factor Authentication
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
`curl -X DELETE -H "Authorization: Bearer ${ACCESS_TOKEN}"http://dev.docu.solutions/api/v1/me/2fa`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 2.9 Retrieve User's Avatar
Downloads the user's avatar image.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/avatar` |
| Method | `GET` |
| Content-Type | `image/jpeg`, `image/png`, `image/gif`, `image/svg` or `image/bmp` |

#### Response 
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` or `204 No Content` (when no avatar is set) |

#### Example Call
`curl -X GET -H "Authorization: Bearer ${ACCESS_TOKEN} "http://dev.docu.solutions/api/v1/me/avatar`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 2.10 Change User's Avatar
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
| avatar | **yes** | Multipart file. |

#### Response 
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X PUT -H "Authorization: Bearer ${ACCESS_TOKEN}" -F "avatar=@/home/user1/Desktop/test.jpg" http://dev.docu.solutions/api/v1/me/avatar`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Either the avatar file was not present, too large (max 256 kilobyte) or neither jpg nor png format. |
| `401 Unauthorized` | This request requires authentication. |

### 2.11 Remove User's Avatar
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
`curl -X DELETE -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/api/v1/me/avatar`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 2.12 Get QR Code for User's OTP
Downloads a 200x200px PNG QR code for the user's OTP secret to pair his Google Authenticator App with it.

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
`curl -X GET -H "Authorization: Bearer ${ACCESS_TOKEN} "http://dev.docu.solutions/api/v1/me/2fa/qr`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |

### 2.13 Get S3 Authorisation Token

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v1/me/storage/token` |
| Method | `POST` |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [Cognito Token](resources.md#4-cognito-token) |
| Status Code | `200` **OK** |

#### CURL
`curl -H $AUTHORIZATION_TOKEN http://dev.docu.solutions/api/v1/me/storage/token`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid user id in path. |
| `401` **Unauthorized** | This endpoint requires full authentication. |


## 3. Registration API
The registration API contains all requests regarding registering a new user (validating email address, issuing registration request, verifying the registration).

**Base-URL:** `/api/v1`

*All requests in this chapter start with this base url!*

### 3.1 Checking if Email Address is Available
This request will check if an email address is still available for a new registration.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/users/email` |
| Method | `GET` |

#### Query Parameters
| Name | Required | Description |
| -------- | ------- | ------- |
| email | **yes** | Email address to check. |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |
| Content-Type | `application/json` |
| Resource | A JSON Boolean, `true` when the email is already in use or `false` when not. |

#### Example Call
`curl -X GET http://dev.docu.solutions/api/v1/users/email?email=<EMAIL_ADDRESS>`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Email address parameter is missing. |

### 3.2 Register a new User
Registers a new user by sending a verification link to the specified email address. The registration can then be completed by hitting the [verification endpoint](#33-verify-new-user).

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/register` |
| Method | `POST` |
| Content-Type | `application/json` |
| Resource | [Registration Resource](#11-registration-resource) |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `201 Created` |

#### Example Call
`curl -X POST  -H "Content-Type: application/json"  -d '{"email": "alexander@partsch.ninja", "organisationName": "docutools", "countryCode": "AT", "language": "de" }' http://dev.docu.solutions/api/v1/register`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Input validation error. |
| `409 Conflict` | The email address is already in use. |

### 3.3 Verify new User
After [registering a new user](#32-register-a-new-user) the user will receive a verification token to the email she specified. This token together with a password can be used to verify the account here.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/register/verify` |
| Method | `POST` |
| Content-Type | `application/json` |
| Resource | [Verification Resource](#12-verification-resource) |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X POST -H "Content-Type: application/json" -d '{"token": "a0f045b5-8b18-438f-8fa9-241eab4dbba7", "password": "helloworld123" }' http://dev.docu.solutions/api/v1/register/verify`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Token is wrong, expired or password to weak. |

## 4. Authentication API
The authentication API contains all requests regarding user login.

### 4.1 Check if Two Factor Authentication is Required
This request returns a boolean, true when a OTP is required when authenticating with the specified email address.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v1/users/email/2fa` |
| Method | `GET` |

#### Query Parameters
| Name | Required | Description |
| -------- | ------- | ------- |
| email | **yes** | Email address to check for. |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |
| Content-Type | `application/json` |
| Resource | JSON boolean, `true` when two factor authentication is required. |

#### Example Call
`curl -X GET http://dev.docu.solutions/api/v1/users/email/2fa?email=alexander@partsch.ninja`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | Email parameter is missing. |

### 4.2 Authenticate a User
Logs in the user with email address, password and optional OTP. If OTP is required can be checked by calling [this endpoints](#41-check-if-two-factor-authentication-is-required).

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/oauth/token` |
| Method | `POST` |
| Content-Type | `multipart/form-data` |

#### Form Data
| Name | Required | Description |
| -------- | ------- | ------- |
| username | **yes** | User's email she registered with. |
| password | **yes** | Her password. |
| code | **no** | Two Factor Authentication Code. |
| grant_type | **yes** | OAuth2 grant types ([see here for more info](http://oauthlib.readthedocs.io/en/latest/oauth2/grants/grants.html), we support `password` and `refresh_token` |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |
| Content-Type | `application/json` |
| Resource | A [JSON Web Token](https://jwt.io/) with at least an access token set. See the [authorization wikipage](#authorization-mechanics) for more info. |
| Basic Authentication | OAuth2 requires you to sent a client id and client secret on authentication. For now we use `tester` and `secret` as username and password in HTTP basic auth header. |

#### Example Call
`curl -X POST -H "Content-Type: multipart/form-data" -F "username=alexander@partsch.ninja" -F "password=helloworld123" -F "grant_type=password" http://tester:secret@dev.docu.solutions/oauth/token`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | When the client credentials are wrong. |

### 4.3 Refresh a Token
Creates a new JWT for the user based on the sent `refresh_token`.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/oauth/token` |
| Method | `POST` |
| Content-Type | `multipart/form-data` |

#### Form Data
| Name | Required | Description |
| -------- | ------- | ------- |
| refresh_token | **yes** | The refresh token value from the users current JWT. |
| grant_type | **yes** | OAuth2 grant types ([see here for more info](http://oauthlib.readthedocs.io/en/latest/oauth2/grants/grants.html), we support `password` and `refresh_token` |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |
| Content-Type | `application/json` |
| Resource | A [JSON Web Token](https://jwt.io/) with at least an access token set. See the [authorization wikipage](#authorization-mechanics) for more info. |
| Basic Authentication | OAuth2 requires you to sent a client id and client secret on authentication. For now we use `tester` and `secret` as username and password in HTTP basic auth header. |

#### Example Call
`curl -X POST -H "Content-Type: multipart/form-data" -F "refresh_token=PREVIOUS_REFRESH_TOKEN" -F "grant_type=refresh_token" http://tester:secret@dev.docu.solutions/oauth/token`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | When the client credentials are wrong or the refresh token is invalid/expired. |

### 4.4 Reset Password
Starts a password reset for the user with the specified email address. The user will receive a new reset link so the [verification endpoint](#33-verify-new-user) can be called again with a new password.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v1/passwordReset` |
| Method | `POST` |
| Content-Type | `application/x-www-form-urlencoded` |

#### Form Data
| Name | Required | Description |
| -------- | ------- | ------- |
| email | **yes** | User's email she registered with. |

#### Response
| Name | Value |
| -------- | ------- |
| Success Status Code | `200` |

#### Example Call
`curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -F "email=alexander@partsch.ninja" http://dev.docu.solutions/api/v1/passwordReset`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | When the email address parameter is not specified. |
