# Organisations API Reference
This page contains the reference documentation for all REST requests around organisations.

## Index
1. [Retrieve an Organisation](#1-retrieve-an-organisation)
2. [Update an Organisation](#2-update-an-organisation)
3. [List all admins in a organisation](#3-list-all-admins-in-a-organisation)

## 1. Retrieve an Organisation
Returns all details of an organisation.

### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v2/organisations/{organisationId}` |
| Method | `GET` ?? |

### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [Organisation Resource](resources.md#1-organisation-resource) |
| Success Status Code | `200` |

### Errors
| Status Code | Description |
| ---------------- | --------------- |
| `401` Unauthorised | This request requires authentication. |
| `404` Not Found | Organisation with this id not found. |

## 3. Update an Organisation
Returns all details of an organisation. Updates the name or vat number of the organisation.

### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v2/organisations` |
| Method | `PATCH` ? |
| Content-Type | `application/json` |
| Resource | [Organisation Resource](resources.md#1-organisation-resource) |
| Required Authorities | admin |

### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [Organisation Resource](resources.md#1-organisation-resource) |
| Success Status Code | `200` |

### Errors
| Status Code | Description |
| ---------------- | --------------- |
| `401` Unauthorised | This request requires authentication. |

## 2. List all admins in a organisation
Returns all admins from an organisation.

### Request
| Name | Value |
| -------- | ------- |
| URL | `/api/v2/organisations/{organisationId}/admins` |
| Method | `GET` ? |
| Content-Type | `application/json` |

### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [Organisation Resource](resources.md#2-user) |
| Success Status Code | `200` |

### Errors
| Status Code | Description |
| ---------------- | --------------- |
| `401` Unauthorised | This request requires authentication. |