# Removed API References
This page contains deprecated/removed API Endpoints and resources from all rest services.

## Index
1. [Add new Address](#1-add-new-address)
2. [Update Address](#2-update-address)
3. [Delete Address](#3-delete-address)
4. [Add Contact](#4-add-contact)
5. [Update Contact](#5-update-contact)
6. [Delete Contact](#6-delete-contact)
7. [Organisation Contact Resource](#7-organisation-contact-resource)
8. [Organisation Address Resource](#8-organisation-address-resource)
## 1. Add new Address
Add a new address to an organisation.

### Path
`POST /api/v1/organisations/{organisationId}/addresses`

### Request Body
```json
        {
            "streetLine": "Optional",
            "zipCode": "Optional",
            "cc": "Optional",
            "homepage": "Optional",
            "type": "HQ, PLANT or BRANCH"
        }
```

### Responses
| Status Code | Description |
| ---------------- | --------------- |
| `200` OK | Address added to organisation, new address in response. |
| `401` Unauthorised | This request requires authentication. |
| `404` Not Found | Organisation with this id not found. |

```json
        {
            "id": "ID of the contact",
            "streetLine": "",
            "zipCode": "",
            "cc": "",
            "homepage": "",
            "type": "HQ, PLANT or BRANCH"
        }
```

## 2. Update Address
Change one or more attributes in an organisation's address.

### Path
`POST /api/v1/organisations/{organisationId}/addresses/{addressId}`

### Request Body
```json
        {
            "streetLine": "Optional",
            "zipCode": "Optional",
            "cc": "Optional",
            "homepage": "Optional",
            "type": "HQ, PLANT or BRANCH"
        }
```

### Responses
| Status Code | Description |
| ---------------- | --------------- |
| `200` OK | Address changed, entity in response. |
| `401` Unauthorised | This request requires authentication. |
| `404` Not Found | Organisation or address with this id not found. |

```json
        {
            "id": "ID of the contact",
            "streetLine": "",
            "zipCode": "",
            "cc": "",
            "homepage": "",
            "type": "HQ, PLANT or BRANCH"
        }
```

## 3. Delete Address
Remove an address from an organisation.

`DELETE /api/v1/organisations/{organisationId}/addresses/{addressId}`

### Responses
| Status Code | Description |
| ---------------- | --------------- |
| `200` OK | Address removed, deleted entity in response. |
| `401` Unauthorised | This request requires authentication. |
| `404` Not Found | Organisation or address with this id not found. |

```json
        {
            "id": "ID of the contact",
            "streetLine": "",
            "zipCode": "",
            "cc": "",
            "homepage": "",
            "type": "HQ, PLANT or BRANCH"
        }
```

## 4. Add Contact
Add new contact to an organisation.

`POST /api/v1/organisations/{organisationId}/contacts`

### Request Body
```json
        {
            "name": "Name of the contact",
            "email": "Email of the contact",
            "type": "Type of the contact, either IT or SALES"
        }
```

### Responses
| Status Code | Description |
| ---------------- | --------------- |
| `200` OK | Created new contact, entity in response. |
| `401` Unauthorised | This request requires authentication. |
| `404` Not Found | Organisation with this id not found. |

```json
        {
            "id": "Contact ID",
            "name": "Name of the contact",
            "email": "Email of the contact",
            "type": "Type of the contact, either IT or SALES"
        }
```

## 5. Update Contact
Update contact fields.

### Path
`PATCH /api/v1/organisations/{organisationId}/contacts/{contactId}`

### Request Body
```json
        {
            "name": "Name of the contact",
            "email": "Email of the contact",
            "type": "Type of the contact, either IT or SALES"
        }
```

### Responses
| Status Code | Description |
| ---------------- | --------------- |
| `200` OK | Contact updated, entity in response. |
| `401` Unauthorised | This request requires authentication. |
| `404` Not Found | Organisation or contact with this id not found. |

```json
        {
            "id": "Contact ID",
            "name": "Name of the contact",
            "email": "Email of the contact",
            "type": "Type of the contact, either IT or SALES"
        }
```

## 6. Delete Contact
Delete a contact.

### Path
`DELETE /api/v1/organisations/{organisationId}/contacts/{contactId}`

### Responses
| Status Code | Description |
| ---------------- | --------------- |
| `200` OK | Contact deleted, entity in response. |
| `401` Unauthorised | This request requires authentication. |
| `404` Not Found | Organisation or contact with this id not found. |

```json
        {
            "id": "Contact ID",
            "name": "Name of the contact",
            "email": "Email of the contact",
            "type": "Type of the contact, either IT or SALES"
        }
```


## 7. Organisation Contact Resource
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

## 8. Organisation Address Resource
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