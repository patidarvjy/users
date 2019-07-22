# Chargebee
The chargebee endpoint serves to provide the chargebee service to customers.

Every organisation which already has a docutools subscription is registered with an ID in chargebee.
If an organisation buys an subscription, it gets automatically registered in the chargebee system,
but the ID still needs to be entered into the docutools system.

The endpoint can only be used by the organisation owners except for the event endpoint.

It is made up of 4 endpoints:

## 1. Info
Get the chargebee info object of the organization of the currently logged in user

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/chargebee-api/v2/info` |
| Method | `GET` |

#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | [Chargebee Info](#chargebee-info) |
| Success Status Code | `200 OK` |

#### Example Call
`curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/chargebee-api/v2/info`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |
| `500 Internal Server Error` | The server failed to get the chargebee info |

## 2. Get checkout
Get the checkout page for a specified product (for organisation which do not have a subscription yet)

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/chargebee-api/v2/checkout/{planId}` |
| Method | `GET` |

#### Query Parameters
| Name | Required | Description |
| -------- | ------------ | --------------- |
| `planId` | **Yes** | The Id of the plan to get the checkout for. |

#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | Chargebee Hosted Page |
| Success Status Code | `200 OK` |

#### Example Call
`curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/chargebee-api/v2/checkout/multi-tool-monthly`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |
| `500 Internal Server Error` | The server failed to get the chargebee info |

## 3. Get portal page
Get the portal page for a registered organisation.

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/chargebee-api/v2/portal` |
| Method | `GET` |

#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Resource | Chargebee Hosted Page |
| Success Status Code | `200 OK` |

#### Example Call
`curl -H "Authorization: Bearer ${ACCESS_TOKEN}" http://dev.docu.solutions/chargebee-api/v2/portal`

#### Errors
| Status Code | Causes |
| ---------------- | --------- |
| `401 Unauthorized` | This request requires authentication. |
| `500 Internal Server Error` | The server failed to get the chargebee info |

## 4. Event callback endpoint

#### Request
| Name | Value |
| -------- | ------- |
| URL | `/chargebee-api/v2/event` |
| Method | `POST` |

#### Query Parameters
| Name | Required | Description |
| -------- | ------------ | --------------- |
| `event` | **Yes** | [Chargebee Event](https://apidocs.chargebee.com/docs/api/events)

#### Response
| Name | Value |
| -------- | ------- |
| Content-Type | `application/json` |
| Success Status Code | `200 OK` |

#### Example Call
`curl -H "Authorization: Bearer ${ACCESS_TOKEN}" -d "chargebeeInfo={"hasChargbeeId"=true, "chargebeeId"="1234567"}" http://dev.docu.solutions/chargebee-api/v2/register `

#### Errors
The endpoint always returns `True` to prevent the chargebee server
to send the request multiple times

## Chargebee Info 
```json
{
  "hasChargebeeAccount": {
    "type": "boolean",
    "nullable": false,
    "readonly": false,
    "description": "Whether the organization is already associated with a chargebee account"
  },
  "chargebeeId": {
    "type": "String",
    "nullable": true,
    "readonly": false,
    "description": "The chargebee id of the organization."
  }
}
```
