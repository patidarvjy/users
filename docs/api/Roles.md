# Roles API
This document contains the *reference documentation* for the creation, management and listing of User Roles.

Each organization has its own set of roles, defined by their owner, administrators and project managers.

A role consists of a name and a set of predefined privileges.

1. [Privileges](#1-Privileges)
  * [1.1 Retrieve all Privileges](#11-retrieve-all-privileges)
2. [Roles API](#2-roles-api)
  * [2.1 List all Roles in User's Organisation](#21-list-all-roles-in-users-organisation)
  * [2.2 List all Roles in an Organisation](#22-list-all-roles-in-an-organisation)
  * [2.3 Create a new Role](#23-create-a-new-role)
  * [2.4 Update Roles](#24-update-roles)
  

## 1. Privileges

| Name | Description | Since |
| ---- | ----------- | ----- |
| `ViewProjectSettings` | Allows the current user to see the projects settings page. | `v1` |
| `ManageProjectSettings` | Allows the current user to update the projects settings page. | `v1` |
| `ViewTeam` | Allows the current user to view all team members of the project. | `v1` |
| `CreateTeamMembers` | Allows the current user to invite new users to the project team. | `v1` |
| `ManageTeam` | Allows the current user to grant and revoke roles to the projects team members and even remove them from the project. | `v1` |
| `ManagePlanFolders` | Allows the user to create, rename and move plan folders in the project. | `v1` |
| `CreatePlans` | Allows the user to import new plans (file or maps) in this project. | `v1` |
| `ManagePlans` | Allows the user to import new revision and update plan metadata (name, activate/deactivte). | `v1` |
| `ViewPins` | Allows the user to see *all* pins in this project. | `v1` |
| `CreatePins` | Allows the user to create new pins in this project. | `v1` |
| `ManagePins` | Allows the user to update *all* pins in this project. | `v1` |
| `ViewTasks` | Allows the user to view *all* tasks in this project. | `v1` |
| `CreateTasks` | Allows the user to create new tasks in this project. | `v1` |
| `ManageTasks` | Allows the user to update *all* tasks in this project. | `v1` |
| `ViewTaskDepedencies` | Allows the user to view dependencies of tasks (also granted when `ViewTasks` is granted). | `v1` |
| `CloseTasks` | When the user is granted this privilege _marking a task as done_ will immediately close the task instead of just setting the done flag. | `v1` |
| `DelegateTasks` | Allows the user to change a task's assignee to another user in the project of the same company, if the current user is the assignee. | `v1` |
| `ViewComments` | Allows the user to vew *all* comments on task's she can access. | `v1` |
| `CreateComments` | Allows the user to post new comments on tasks. | `v1` |
| `ManageComments` | Allows the user to update *all* comments on tasks. | `v1` |
| `ViewMedia` | Allows the user view *all* media on pins. | `v1` |
| `CreateMedia` | Allows the user to create any kind of media. | `v1` |
| `ManageMedia` | Allows the user to update *all* media on pins. | `v1` |
| `ViewPhotos` | Allows the user to see photo media. | `v1` |
| `CreatePhotos` | Allows the user to upload photo media. | `v1` |
| `ViewAudios` | Allows the user to see audio media. | `v1` |
| `CreateAudios` | Allows the user to upload audio media. | `v1` |
| `ViewVideos` | Allows the user to upload video media. | `v1` |
| `CreateVideos` | Allows the user to upload video media. | `v1` |
| `ViewSketches` | Allows the user to view sketches. | `v1` |
| `CreateSketches` | Allows the user to upload sketched media. | `v1` |
| `ViewText` | Allows the user to view text. | `v1` |
| `CreateText` | Allows the user to create text media. | `v1` |
| `ViewHistory` | Allows the user to view a pin's history. | `v1` |
| `ViewDatasets` | Allows the user to see *all* datasets. | `v1` |
| `CreateDatasets` | Allows the user to create new datasets. | `v1` |
| `ManageDatasets` | Allows the user to update *all* datasets. | `v1` |
| `ViewGroups` | Allows the user to view *all* groups. | `v1` |
| `CreateGroups` | Allows the user to create new groups. | `v1` |
| `ManageGroups` | Allows the user to update *all* groups. | `v1` |
| `ViewReports` | Allows the user to see *all* report templates. | `v1` |
| `CreateReports` | Allows the user to create new reports. | `v1` |
| `ManageReports` | Allows the user to create and update *all* report templates. | `v1` |
| `RejectTasks` | Allows the user to reject Tasks. | `v1` |

### 1.1 Retrieve all Privileges
This endpoint returns all privileges as *string array*.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/privileges` |
| Method | `GET` |
| Required Authorities | *none* |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | Array of Strings |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Aauthorization $AUTHORIZATION_TOKEN" http://dev.docu.solutions/api/v2/privileges`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |

## 2. Roles API

### 2.1 List all Roles in User's Organisation

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/roles` |
| Method | `GET` |
| Required Authorities | *none* |

#### Query Parameters
| Name | Required (y/n) | Description |
| ---- | -------------- | ----------- |
| `sort` | *no* (Default: `name`) | Property to sort results after (`name`, `lastModified`, `createdBy`, `activeProjects`). |
| `sortDirection` | *no* (Default: `ASC`) | In which direction to sort (`ASC`, `DESC`). |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | Array of [Roles](resources.md#12-user-role) |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Authorization $AUTHORIZATION_TOKEN" http://dev.docu.solutions/api/v2/roles`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |

### 2.2 List all Roles in an Organisation

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/organisations/{organisationId}/roles` |
| Method | `GET` |
| Required Authorities | *none* |

#### Query Parameters
| Name | Required (y/n) | Description |
| ---- | -------------- | ----------- |
| `sort` | *no* (Default: `name`) | Property to sort results after (`name`, `lastModifed`, `createdBy`, `activeProjects`). |
| `sortDirection` | *no* (Default: `ASC`) | In which direction to sort (`ASC`, `DESC`). |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | Array of [Roles](#21-roles-resource) |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Authorization $AUTHORIZATION_TOKEN" http://dev.docu.solutions/api/v2/organisations/{organisationId}/roles`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `404` **Not Found** | No organisation with this ID found. |

### 2.3 Create a new Role
**Note**: Creation of new rules has been disabled, calling this API will result in a BadRequestException.
Creates a new role in the user's organisation.

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v1/roles` |
| Method | `POST` |
| Required Authorities | `admin`, `projectCreator` |
| Content Type | `application/json` |
| Resource | [Roles](resources.md#12-user-role) |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | Created [Role](resources.md#12-user-role) |
| Status Code | `201` **Created** |

#### CURL
`curl -H "Authorization $AUTHORIZATION_TOKEN" -H "Content-Type: application/json" -X POST -d '{"name": "My Role", "privileges" ["ManageTeam", "CreatePlanFolders"]}' http://dev.docu.solutions/api/v2/roles`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Role name is missing or has more than 64 characters, unknown privilege in the body. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | User does not have the required authorities. |

### 2.4 Update Roles

#### Request
| Name | Value |
| ---- | ----- |
| Path | `/api/v2/roles/{roleId}` |
| Method | `PATCH` |
| Required Authorities | `admin`, `projectCreator` |
| Content Type | `application/json` |
| Resource | [Roles](resources.md#12-user-role) |

#### Response
| Name | Value |
| ---- | ----- |
| Content-Type | `application/json` |
| Resource | Updated [Role](resources.md#12-user-role) |
| Status Code | `200` **OK** |

#### CURL
`curl -H "Aauthorization $AUTHORIZATION_TOKEN" -H "Content-Type: application/json" -X POST -d '{"name": "My Role", "privileges" ["ManageTeam", "CreatePlanFolders"]}' http://dev.docu.solutions/api/v2/roles/1fb64bb2-536d-43e6-9cb9-156022d2dde5`

#### Errors
| Status Code | Description |
| ----------- | ----------- |
| `400` **Bad Request** | Role name has more than 64 characters or unknown privilege in the body. |
| `401` **Unauthorized** | This endpoint requires full authentication. |
| `403` **Forbidden** | User does not have the required authorities. |
| `404` **Not Found** | No role with this ID exists. |