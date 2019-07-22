# Privileges API

## 1. Privilege Check
```json
{
  "privileges": {
    "type": "String Array",
    "nullable": false,
    "readonly": true,
    "description": "List of privileges that were checked."
  },
  "projectId": {
    "type": "UUID",
    "nullable": false,
    "readonly": true,
    "description": "ID of the project that the privileges where checked against."
  },
  "currentUserId": {
    "type": "UUID",
    "nullable": false,
    "readonly": true,
    "description": "ID of the current User."
  },
  "any": {
    "type": "boolean",
    "nullable": false,
    "readonly": true,
    "description": "Whether the user has all the privileges (any=false) or at least one (any=true)."
  },
  "check": {
    "type": "boolean",
    "nullable": false,
    "readonly": true,
    "description": "Whether the user has these privileges or not."
  }
}
```

## 2. Check Privilege
Checks if the user has one or many privileges in a project.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v1/me/checkPrivilege` |
| Method | `GET` |

| Name | Required | Description |
| -------- | ------------ | --------------- |
| projectId | **yes** | ID of the project to get the privileges for. |
| privileges | **yes** | Array of privileges. |
| any | **no** (Default: `false`) | `true` or `false` value if the user has to have any of the specified privileges or all of them. |

#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [Privilege Check](#1-privilege-check) |
| Success Status Code | `200 OK` |

#### Example Call
`curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/api/v1/me/checkPrivilege?projectId=1&privileges=UPDATE_TASKS,SEE_TASKS&any=true`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `400 Bad Request` | The project id is invalid or the parameter is missing. |
| `401 Unauthorized` | This request requires authentication. |