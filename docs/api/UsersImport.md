# User Import API
Endpoints for importing users from files to the current users organisation.

## 1. Upload New File
Uploads a new file and saves it temporarly for the actual import request.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/imports/files` |
| Method | `POST` |
| Required Authorities | `admin` |
| Content-Type | `multipart/form-data` |

#### Request Parts
| Name | Content-Type |
| ---- | ----- |
| `file` | `text/csv`|

#### Request Params
| Name | Description |
| ---- | ----- |
| `delimiter` | Csv file column separator, default value is `,`|
| `langauge` | Language in which file is written. For instance `german`, `english`, default value is `user profile language`|

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |
| Resource | [Imported File](resource.md#6-imported-file-dto) |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X POST -F "file=@users.csv" http://dev.docu.solutions/api/v2/users/imports/files?delimiter=;`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid content type. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin`, the users have to be in the same organisation |
| `413` **Payload Too Large** | Image exceeds file size limit of 3.2MB. |

## 2. Import Users from Uploaded File
Takes a file ID and a column mapping and imports the users from the file into the current users organisation as unlicensed users with not permissions.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/users/imports/{fileId}` |
| Method | `POST` |
| Required Authorities | `admin` |
| Content-Type | `application/json` |
| Resource | Mapping of File Column Names to User Attributes (see List below). |

#### Request Params
| Name | Description |
| ---- | ----- |
| `delimiter` | Csv file column separator, default value is `,`|
| `skipFirstRow` | Skip first row in Csv file, type `boolean`, default value is `true`|
| `fileEncoding` | File encoding type, returned in response of first call of import api, default value `UTF-8`|

#### User Attributes
* `FirstName`
* `LastName`
* `Name`: When containing a Whitespace in between characters will be split up in first and last name, otherwise only first name.
* `Phone`
* `Fax`
* `JobTitle`
* `Department`
* `InternalId`
* `Email`: Required.
* `Username`:
* `Street`
* `CompanyName`
* `Zip`
* `City`
* `Active`: When the column is not present all users are active, when present then only the users who have a value in this column (not empty or whitespace) are active.
* `Ignore`: Columns mapped to this attribute are ignored in the import.

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |
| Resource | List of [Docutools Users](resources.md#2-user) |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X POST -d '{"Email Address": "Email", "Name": "LastName"}' http://dev.docu.solutions/api/v2/users/imports/4954a286-13d4-4d49-9939-042b736a210c?delimiter=;`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid content type, file is corrupted or missing required property (either `Email` or `Username`). |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be `admin`, the users have to be in the same organisation |