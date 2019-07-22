# Contact API
This document contains the *Reference Documentation* for the API Endpoints creating, retrieving and updating contacts of a project.

## Index
1. [Create Contact](#1-create-contact)
2. [Get Contact](#2-get-contact)
3. [Update Contact](#3-update-contact)
4. [Delete Contact](#4-delete-contact)
5. [List Contacts](#5-list-contacts)
6. [Export Contact as VCard](#6-export-contact-as-vcard)
7. [Upload Import Contact Csv](#7-upload-import-contact-csv)
8. [Import Contacts](#8-import-contacts)
9. [Project Import Resource](#9-project-import-resource)
10. [Export Contacts as CSV](#10-export-contacts-as-csv)

### 1. Create Contact
Requests has to include atleast one of the following attributes: `email`, `companyName`, `lirstName`, `lastName`.
Required is also the `projectId` in the Project Contact Resource.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/contacts` |
| Method | `POST` |
| Request Body | [Project Contact](resource.md#5-project-contact) |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [Project Contact](resource.md#5-project-contact) |
| Status Code | `200` **OK** |

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |

### 2. Get Contact

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/contacts/{id}` |
| Method | `GET` |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [Project Contact](resource.md#5-project-contact) |
| Status Code | `200` **OK** |

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `404` **Not Found** | No contact found under the given id. |

### 3. Update Contact

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/contacts/{id}` |
| Method | `PATCH` |
| Request Body | [Project Contact](resource.md#5-project-contact) |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [Project Contact](resource.md#5-project-contact) |
| Status Code | `200` **OK** |

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `404` **Not Found** | No contact found under the given id. |

### 4. Delete Contact

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/contacts/{id}` |
| Method | `DELETE` |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | [Project Contact](resource.md#5-project-contact) |
| Status Code | `200` **OK** |

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |

### 5. List Contacts

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/contacts` |
| Method | `GET` |

#### Request Parameters
| Name | Type | Required | Description |
| ---- | ---- | -------- | ----------- |
| projectId | UUID | **true** | The id of the project to list contacts of. |
| search | String | false | The string to search for, when listing all contacts. |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | Array of [Project Contacts](resource.md#5-project-contact) |
| Status Code | `200` **OK** |

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |

### 6. Export Contact as VCard

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/contacts/{id}/vcard` |
| Method | `GET` |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `text/x-vcard` |
| Status Code | `200` **OK** |

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user is not allowed to export the given contact as vcard. |


### 7. Upload Import Contact Csv
Uploads a new file and saves it temporarly for the actual import request.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/contacts/import/file` |
| Method | `POST` |
| Required Authorities | `ManageTeam` |
| Content-Type | `multipart/form-data` |

#### Request Parts
| Name | Content-Type |
| ---- | ----- |
| `file` | `text/csv`|

#### Request Params
| Name | Description |
| ---- | ----- |
| `delimiter` | Csv file column separator, default value is `,`|
| `projectId` | Project id for which contacts will be imported `Required`|
| `language` | Language in which file is written. For instance `german`, `english`, default value is `user profile language`|

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |
| Resource | [Imported File](resource.md#6-imported-file-dto) |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X POST -F "file=@contacts.csv" http://dev.docu.solutions/api/v2/contacts/import/file?projectId=projectId&delimiter=;`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid content type. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be permissible to `ManageTeam`|

### 8. Import Contacts
Takes a file ID and a column mapping and imports the contacts from the file into the project contacts.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/contacts/import` |
| Method | `POST` |
| Required Authorities | `ManageTeam` |
| Content-Type | `application/json` |
| Resource | [Project Import Resource](resource.md#9-project-import-resource) |

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |
| Resource | List of [Project Contact](resource.md#5-project-contact) |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X POST -d '{"fileId":"fileId","projectId":"projectId","columnsMap":{Email Address": "Email", "Name": "LastName", "CompanyName":"CompanyName"}}' http://dev.docu.solutions/api/v2/contacts/import`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid content type, file is corrupted or missing required property (either `Email` or `Username`, `CompanyName` and (`Name` or `FirstName` or `LastName`)). |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be permissible to `ManageTeam`|


### 9. Copy Contacts
Copy a contacts from one project to another.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/contacts` |
| Method | `PUT` |
| Required Authorities | `ManageTeam`, `ViewTeam` |

#### Request Params
| Name | Description |
| ---- | ----- |
| `from` | The id of the project where the contacts should be copied from.|
| `to` | The id of the project where the contact should be copied to.|

#### Response
| Name | Value |
| ---- | ----- |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Bearer $AUTHORIZATION_TOKEN" -X PUT http://dev.docu.solutions/api/v2/contacts?from=fromId&to=toId`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Invalid content type. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user has to be permissible to `ManageTeam` and `ViewTeam`|

### 10. Export Contacts as CSV

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/contacts/{projectId}/csv` |
| Method | `GET` |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `text/csv` |
| Status Code | `200` **OK** |

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | The user is not allowed to export the given contact as vcard. |