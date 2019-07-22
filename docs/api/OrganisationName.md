# Organisation Name API Reference
This page contains the reference documentation for all REST requests around organisation names.

## Index
1. [Create an Organisationname](#1-create-an-organisationname)
2. [Change an Organisationname](#2-change-an-organisationname)
3. [List the Organisationnames](#2-list-the-organisationnames)
4. [Get an Organisationname](#2-get-an-organisationname)
5. [Delete an Organisationname](#2-delete-an-organisationname)


## 1. Create an Organisationname
Creates an Organisationname for an existing Organisation..

### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v2/me/organisation/names` |
| Method | `POST` |
| Resource | [Organisation Resource](resources.md#7-organisation-name) |

### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [Organisation Resource](resources.md#7-organisation-name) |
| Success Status Code | `201 Created` |

### Errors
| Status Code | Description |
| ---------------- | --------------- |
| `401` Unauthorised | This request requires authentication. |
| `404` Not Found | Organisation with this id not found. |
| `409` Conflict | The given name already exists for the given organisation. |


## 2. Change an Organisationname
Change an existing Organisationname.

### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v2/me/organisation/names/{organisationNameId}` |
| Method | `PUT` |
| Resource | [Organisation Resource](resources.md#7-organisation-name) |

### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [Organisation Resource](resources.md#7-organisation-name) |
| Success Status Code | `200` |

### Errors
| Status Code | Description |
| ---------------- | --------------- |
| `401` Unauthorised | This request requires authentication. |
| `404` Not Found | Organisation with this id not found. |
| `409` Conflict | The given name already exists for the given organisation. |


## 3. List the organisationnames
List all organisationnames of your organisation.

### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v2/me/organisation/names` |
| Method | `GET` |

### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [Organisation Resource](resources.md#7-organisation-name) |
| Success Status Code | `200` |

### Errors
| Status Code | Description |
| ---------------- | --------------- |
| `401` Unauthorised | This request requires authentication. |


## 4. Get an Organisationname
Get an Organisationname by ID.

### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v2/me/organisation/names/{organisationNameId}` |
| Method | `GET` |

### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [Organisation Resource](resources.md#7-organisation-name) |
| Success Status Code | `200` |

### Errors
| Status Code | Description |
| ---------------- | --------------- |
| `401` Unauthorised | This request requires authentication. |
| `404` Not Found | No Organisationname was found under the given ID. |


## 5. Delete an Organisationname
Delete an Organisationname by ID.

### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v2/me/organisation/names/{organisationNameId}` |
| Method | `DELETE` |

### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [Organisation Resource](resources.md#7-organisation-name) |
| Success Status Code | `200` |

### Errors
| Status Code | Description |
| ---------------- | --------------- |
| `401` Unauthorised | This request requires authentication. |
| `404` Not Found | No Organisationname was found under the given ID. |
