# Assignees API

1. [Resources](#1-resources)
  * [1.1 Assignee](#11-assignee)
  * [1.2 Company](#12-company)
  * [1.3 Craft](#13-craft)
2. [List All Assignees](#2-list-all-assignees)
3. [List All Companies](#3list-all-companies)
4. [List Crafts](#4-list-crafts)

## 1. Resources

### 1.1 Assignee
A project member or person contact that can be assigned in this project.

```json
{
    "id": {
      "type": "uuid",
      "nullable": false,
      "readonly": true,
      "description": "Unique ID of the assignee."
    },
    "name": {
      "type": "String",
      "nullable": false,
      "readonly": true,
      "description": "The assignees first and last name."
    },
    "companyId": {
      "type": "uuid",
      "nullable": false,
      "readonly": true,
      "description": "Unique ID of the company the assignee belongs too."
    },
    "type": {
      "type": "String (Enum)",
      "nullable": false,
      "readonly": true,
      "values": ["User", "Contact"],
      "description": "The type of the assignee."
    },
    "companyName": {
      "type": "String",
      "nullable": false,
      "readonly": true,
      "description": "Name of the company the assignee belongs too."
    },
    "lastModified": {
      "type": "Instant",
      "readOnly": true,
      "description": "ISO Date when User was updated."
    },
    "state":{
      "type":"String",
      "nullable": false,
      "readonly": true,
      "values": ["Active", "Inactive", "Invited", "Removed"],
      "decription": "The state of the assignee."
    }
}
```

### 1.2 Company
A company participating in this project (there must be at least one [Assignee](#17-assignee) belonging to this company).

```json
{
  "id": {
    "type": "uuid",
    "nullable": false,
    "readonly": true,
    "description": "Unique ID of the company."
  },
  "name": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "description": "The companies name."
  },
  "defaultAssignee": {
    "nullable": false,
    "description": "The user which shall be assigned by default when the company is choosen.",
    "id": {
      "type": "uuid",
      "nullable": false,
      "readonly": true,
      "description": "Unique ID of the assignee."
    },
    "name": {
      "type": "String",
      "nullable": false,
      "readonly": true,
      "description": "The assignees first and last name."
    },
    "companyId": {
      "type": "uuid",
      "nullable": false,
      "readonly": true,
      "description": "Unique ID of the company the assignee belongs too."
    },
    "companyName": {
      "type": "String",
      "nullable": false,
      "readonly": true,
      "description": "Name of the company the assignee belongs too."
    },
    "type": {
      "type": "String (Enum)",
      "nullable": false,
      "readonly": true,
      "values": ["User", "Contact"],
      "description": "The type of the assignee."
    },
    "lastModified": {
       "type": "Date",
       "readOnly": true,
       "description": "ISO Date when User was updated."
     },
    "state":{
      "type":"String",
      "nullable": false,
      "readonly": true,
      "values": ["Active", "Inactive", "Invited", "Removed"],
      "decription": "The state of the assignee."
    }
  },
  "lastModified": {
       "type": "Date",
       "readOnly": true,
       "description": "ISO Date when User was updated."
   }
}
```

### 1.3 Craft
**NOTE**: This currently hasn't been implemented yet.
Crafts categorize companies participating in a project (examples for crafts: *Electricians*, *Painter*, ...).

```json
{
  "id": {
    "type": "uuid",
    "nullable": false,
    "readonly": true,
    "description": "Unique ID of the craft."
  },
  "name": {
    "type": "String",
    "nullable": false,
    "readonly": true,
    "description": "The craft's name."
  }
}
```

### 2. List All Assignees
Get a list of all assignees in this project, optionally filtered by company.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v2/projects/{projectId}/assignees/all` |
| Method | `GET` |
| Required Authorities | *projectCreator*, *admin* in the project's organisation or privileges `CreateTask` or `ManageTasks` |

#### Query Parameters
| Name | Required | Description |
| -------- | ------------ | --------------- |
| `company` | **No** | ID of the company to filter for. |
| `since` | **No** | Only show Assignees that are modified after the given timestamp. |


#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | Array of [Assignees](#11-assignee) |
| Success Status Code | `200 OK` |

#### Example Call
`curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/api/v2/projects/1/assignees/all?company=12`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400` **Bad Request** | The project id or parameter is invalid. |
| `401` **Unauthorized** | This request requires authentication. |
| `403` **Forbidden** | The user has no privilege to list this project's assignees. |
| `404` **Not Found** | Project not found. |

### 3. List All Companies
**NOTE**: The craft functionality hasn't been implemented yet.
Get a list of all companies having employees that participate in this project.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v2/projects/{projectId}/companies/all` |
| Method | `GET` |
| Required Authorities | *projectCreator*, *admin* in the project's organisation or privileges `CreateTask` or `ManageTasks` |

#### Query Parameters
| Name | Required | Description |
| -------- | ------------ | --------------- |
| `craft` | **No** | ID of the craft to filter for. |
| `since` | **No** | Only show Companies that are modified after the given timestamp. |


#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | Array of [Companies](#12-company) |
| Success Status Code | `200 OK` |

#### Example Call
`curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/api/v2/projects/1/companies/all?company=12`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400` **Bad Request** | The project id or parameter is invalid. |
| `401` **Unauthorized** | This request requires authentication. |
| `403` **Forbidden** | The user has no privilege to list this project's assignee companies. |
| `404` **Not Found** | Project not found. |

### 4. List Crafts
**NOTE**: The endpoint functionality hasn't been implemented yet. (Currently returning an empty list)
Gets all crafts of the companies participating in this project.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v2/projects/{projectId}/crafts/all` |
| Method | `GET` |

#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | Array of [Crafts](#13-craft) |
| Success Status Code | `200 OK` |

#### Example Call
`curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/api/v2/projects/1/crafts/all`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400` **Bad Request** | The project id is invalid. |
| `401` **Unauthorized** | This request requires authentication. |
| `404` **Not Found** | Project not found. |