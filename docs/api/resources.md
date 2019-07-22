# docutools Users - Resources
This document defines all resources provided and accepted by the docutools user service REST API.

## 1. Organisation
```json
{
  "id": {
    "type": "uuid",
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
  "billingEmail": {
    "type": "String",
    "nullable": false,
    "description": "The billing email of this organisation (by default owner)."
  },
  "subscription": {
    "type": "Subscription (See #13)",
    "nullable": false,
    "description": "The subscription of the organisation."
  },
  "created": {
    "type": "Date",
    "readOnly": true,
    "description": "ISO Date when Organization was created."
  },
  "lastModified": {
    "type": "Date",
    "readOnly": true,
    "description": "ISO Date when Organization was updated."
  },
  "noLicenseMessages": {
    "type": "Map of String to String",
    "nullable": true,
    "readOnly": true,
    "description": "Map of language codes to messages, shown when an organisation user has no license."
  },
  "idpLink":{
    "type": "String",
    "nullable": true,
    "readOnly": true,
    "description": "idpLink of the organisation, when you are the organisation owner."
  }
}
```

## 2. User 
Represents a docutools user profile.

```json
{
  "userId": {
    "type": "uuid",
    "nullable": false,
    "readonly": true,
    "description": "Unique ID of this user."
  },
  "organisationId": {
    "type": "uuid",
    "nullable": false,
    "readonly": true,
    "description": "Reference ID to the users organisation."
  },
  "organisationNameId": {
    "type": "uuid",
    "nullable": false,
    "readonly": true,
    "description": "Reference ID to the users organisation name or the id of the organisation if the user has no custom organisation name."
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
  "email": {
      "type": "String",
      "nullable": false,
      "readonly": true,
      "constraints": "Non empty, max 128 characters, must contain '@' symbol.",
      "description": "The users email address."
    },
  "phone": {
    "type": "String",
    "nullable": true,
    "constraints": "Max 32 characters.",
    "description": "The user's telephone number."
  },
  "fax": {
      "type": "String",
      "nullable": true,
      "constraints": "Max 128 characters.",
      "description": "The user's fax number."
    },
  "jobTitle": {
    "type": "String",
    "nullable": true,
    "constraints": "Max 128 characters.",
    "description": "The user's job title/description."
  },
  "department": {
      "type": "String",
      "nullable": true,
      "constraints": "Max 128 characters.",
      "description": "The user's department name."
    },
  "internalId": {
      "type": "String",
      "nullable": true,
      "constraints": "Max 128 characters.",
      "description": "The user's internal comapny id."
    },
  "street": {
        "type": "String",
        "nullable": true,
        "constraints": "Max 128 characters.",
        "description": "The user's street address."
      },
  "zip": {
        "type": "String",
        "nullable": true,
        "constraints": "Max 32 characters.",
        "description": "The user's zip code."
      },
  "city": {
        "type": "String",
        "nullable": true,
        "constraints": "Max 128 characters.",
        "description": "The user's city name."
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
  "owner": {
    "type": "Boolean",
    "nullable": false,
    "readonly": true,
    "description": "Whether the user is owner of the organisation or not."
  },
  "active": {
    "type": "Boolean",
    "nullable": false,
    "readonly": true,
    "description": "Whether the user can login or not. For organisation admins to lock out users."
  },
  "verified": {
    "type": "boolean",
    "nullable": false,
    "readonly": true,
    "description": "Whether the user is email verified or not."
  },
  "license": {
   "type": "Object",
   "nullable": false,
   "readonly": true,
   "description": "Tells whether the user has a license or not and for how long",
   "content": {
    "type": {
      "type": "String / Enumeration",
      "nullable": false,
      "values": ["Master", "Multi", "Combo", "Pocket", "Test"],
      "description": "Describes what access the user has to docutools."
    },
    "since": {
      "type": "Datetime",
      "nullable": true,
      "description": "Since when the user has this license type."
    },
    "until": {
      "type": "Datetime",
      "nullable": true,
      "description": "Until when the user holds this license."
    },
    "paid": {
      "type": "boolean",
      "nullable": true,
      "description": "Whether the license is paid."
    }
   }
  },
  "permissions": {
    "type": "List of Strings",
    "values": "CreateProjects, CreateProjectFolders, ViewUsers, EditUsers, GrantAdmin, EditOrganisationSettings, ViewReportTemplates, EditReportTemplates, ViewMaestro, ManageLicenses",
    "readonly": "true",
    "description": "The permissions of the user for its organisation."
  },
  "created": {
       "type": "Date",
       "readOnly": true,
       "description": "ISO Date when User was created."
   },
  "lastModified": {
       "type": "Date",
       "readOnly": true,
       "description": "ISO Date when User was updated."
  },
  "invitationStatus":{
    "type":"String",
    "readOnly": true,
    "description": "Invitation status of user, Values['Pending','Active','Inactive']"
  },
  "type": {
   "type": "Enum (Password, SAML)"
  }
  "noLicenseMessage": {
   "type": "String",
   "readOnly": true,
   "description": "A message displayed to the user when he has no license and no projects."
  },
  "privacyPolicyAccepted": {
    "type": "Boolean",
    "readOnly": true,
    "description": "User accepted privacy policy"
  },
  "termsAndConditionsAccepted": {
                                  "type": "Boolean",
                                  "readOnly": true,
                                  "description": "User accepted terms and conditions"
                                },
}
```

### 2.1 User Batch Update
```json
{
  "ids": {
    "type": "Array of Strings (UUIDs)",
    "description": "IDs of the users to update."
  },
  "update": {
    "type": "Object (User)",
    "description": "User Properties to update."
  }
}
```

## 3. Registration
The following resources are used in the registration and verification process for new user accounts.

### 3.1 Registration Resource
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

### 3.2 Verification Resource

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

## 4. Cognito Token
```json
{
    "token": {
        "type": "String",
        "description": "The s3 token."
        "readonly": true
    },
    "identityId": {
        "type": "String",
        "description": "The identity id."
        "readonly": true
    }
}
```

## 5. Project Contact

```json
{
    "id" : {
        "type": "UUID",
        "description": "id of the contact",
        "readonly": "yes",
        "required": false
    },
    "projectId" : {
        "type": "UUID",
        "description": "id of the project the contact belongs to",
        "readonly": false,
        "required": false
    },
    "email" : {
        "type": "String",
        "description": "Email of the contact",
        "readonly": false,
        "required": false
    },
    "companyName" : {
        "type": "String",
        "description": "Name of the company",
        "readonly": false,
        "required": false
    },
    "firstName" : {
        "type": "String",
        "description": "First name of the contact",
        "readonly": false,
        "required": false
    },
    "lastName" : {
        "type": "String",
        "description": "Last name of the contact",
        "readonly": false,
        "required": false
    },
    "phone" : {
        "type": "String",
        "description": "Phone number of the contact",
        "readonly": false,
        "required": false
    },
    "fax" : {
        "type": "String",
        "description": "Fax number of the contact",
        "readonly": false,
        "required": false
    },
    "jobTitle" : {
        "type": "String",
        "description": "Job title of the contact",
        "readonly": false,
        "required": false
    },
    "department" : {
        "type": "String",
        "description": "Department of the contact",
        "readonly": false,
        "required": false
    },
    "internalId" : {
        "type": "String",
        "description": "Internal id of the contact",
        "readonly": false,
        "required": false
    },
    "street" : {
        "type": "String",
        "description": "Street address of the contact",
        "readonly": false,
        "required": false
    },
    "zip" : {
        "type": "String",
        "description": "Zip code of the contact",
        "readonly": false,
        "required": false
    },
    "city" : {
        "type": "String",
        "description": "City of the contact",
        "readonly": false,
        "required": false
    },
    "created" : {
        "type": "Datetime",
        "description": "When this contact was created.",
        "readonly": true,
        "required": false
    },
    "lastModified" : {
        "type": "Datetime",
        "description": "When this contact was last modified.",
        "readonly": true,
        "required": false
    },
    "replaced" : {
        "type": "boolean",
        "description": "Whether this contact is pointing to an existing docutools user instead.",
        "readonly": true,
        "required": false
    },
    "permissions" : {
        "type": "Array of Strings",
        "values": [
            "Edit",
            "View",
            "Delete"
        ],
        "description": "The permissions the current user has for this contact"
        "readonly": true,
        "required": false
    }
}
```

## 6. Imported File DTO
```json
{
  "id": {
    "type": "String (UUID)",
    "description": "The ID of this uploaded file to later reference in the actual import Endpoint."   
  },
  "mimeType": {
    "type": "String",
    "description": "MIME Type of the uploaded file (for now always text/csv)."
  },
  "sizeInBytes": {
    "type": "Numeric (Long)",
    "description": "Size of the uploaded file in bytes."
  },
  "savedUntil": {
    "type": "String (Timestamp)",
    "description": "Since this import job is temporary these files are only kept until a certain time."
  },
  "columns": {
    "type": "Array of Strings",
    "description": "Column row of the uploaded file."
  },
  "delimiter":{
    "type": "char",
    "description": "Csv file column separator"
  },
  "fileEncoding":{
    "type": "string",
    "description": "Csv file endcoding type, for use in second call of import users or contacts"
  }
}
```

## 7. Organisation Name
```json
{
  "id": {
    "type": "String (UUID)",
    "description": "The ID of the Organisationname"
  },
  "name": {
    "type": "String",
    "description": "The custom organisation name."
  },
  "organisation": {
    "type": "Organisation Resource (See #1)",
    "description": "The organisation the name is linked to."
  },
  "permissions": {
    "type": "List of Strings",
    "values": ["Delete"],
    "description": "The user permission on the organisation name."
  }
}
```

## 8. License
```json
{
  "type": {
    "type": "String",
    "description": "The type of the license. (Subscription Type)",
    "values:" ["Master", "Multi", "Combo", "Pocket", "Test"]
  },
  "since": {
    "type": "Datetime",
    "nullable": true,
    "description": "Since when the user has this license."
  },
  "until": {
    "type": "Datetime",
    "nullable": true,
    "description": "Until when the user holds this license."
  },
  "paid": {
    "type": "boolean",
    "description": "Whether this license is paid or not."
  }
}
```

### 9. Project Import Resource
```json
{
  "fileId": {
    "type": "String (UUID)",
    "description": "The ID of this uploaded file(Storage Id) in `/import/file` endpoint.",
    "required":true
  },
  "projectId": {
      "type": "String (UUID)",
      "description": "The ID of project in which contact will imported.",
      "required":true
   },
  "delimiter":{
    "type": "char",
    "description": "Csv file column separator",
    "defaultValue":",",
    "required":false
  },
  "columnsMap":{
      "type":"Map<String,Attribute>",
      "description":"Mapping of File Column Names to Project Contact Attributes (see List below).",
      "required":true
  },
  "fileEncoding":{
      "type": "string",
      "description": "File encoding type, returned in response of first call of import api, default value `UTF-8`"
  }
}
```

#### Project Contact Attributes
* `FirstName`
* `LastName`
* `Name`: When containing a Whitespace in between characters will be split up in first and last name, otherwise only first name.
* `Phone`
* `Fax`
* `JobTitle`
* `Department`
* `InternalId`
* `Email`: Partially Required.
* `Username`: Partially Required.
* `CompanyName`: Partially Required
* `Street`
* `Zip`
* `City`
* `Ignore`: Columns mapped to this attribute are ignored in the import.

**In a project contact "Email or Username, Name(any FirstName, LastName or Name) or CompanyName are necessary"


## 10. User Account
```
{
    "id": {
        "type": "UUID",
        "nullable": false,
        "readonly": true,
        "description": "The id of the account"
    },
    "activated": {
        "type": "LocalDate",
        "nullable": false,
        "readonly": true,
        "description": "The timestamp of when the account was activated"
    },
    "active": {
        "type": "boolean",
        "nullable": false,
        "readonly": true,
        "description": "Whether the account is active or not"
    },
    "type": {
        "type": "String (Enum)",
        "nullable": false,
        "readonly": true,
        "values": ["Master", "Multi", "Combo", "Pocket", "Test"],
        "description": "The type of the account"
    },
    "until": {
        "type": "LocalDate",
        "nullable": false,
        "readonly": true,
        "description": "The expiration date of the license of this account"
    },
}
```

## 11. Invite Members in Bulk Resource
```json
{
  "emails": {
    "type": "List of Strings",
    "nullable": true,
    "description": "Email addresses of new users to be invited."
  },
  "userIds": {
    "type": "List of UUIDs",
    "nullable": true,
    "description": "UUIDs of existing docutools users to be invited."
  },
  "active": {
    "type": "Boolean",
    "nullable": false,
    "description": "If the newly invited members shall be active or not."
  },
  "roleId": {
    "type": "UUID",
    "nullable": false,
    "description": "The ID of the role the new members will be granted."
  },
  "welcomeMessage": {
    "type": "String",
    "nullable": true,
    "description": "Will be included in the notification or E-Mail inviting these new users."
  }
}
```

### Example
```json
{
  "emails": ["john@example.com", "peter@example.com"],
  "userIds": [
    "fbc64edc-5e7a-402c-a25a-a4936dd7e737",
    "055debb1-8eee-406b-964a-38124bfb8342"
  ],
  "active": true,
  "roleId": "49f42116-be1c-4330-ac66-7f55e27e68e1"
}
```

## 12. User Role
```json
{
  "id": {
    "type": "String (UUID)",
    "nullable": false,
    "readonly": true,
    "description": "The unique id of this role."
  },
  "name": {
    "type": "String",
    "nullable": false,
    "constraints": "Max. 64 characters.",
    "description": "The role's user-friendly name."
  },
  "privileges": {
    "type": "Array of Strings (Privileges)",
    "nullable": false,
    "description": "Privileges of this role (see list above)."
  },
  "createdBy": {
    "id": {
      "type": "String (UUID)",
      "nullable": false,
      "readonly": true,
      "description": "ID of the user who created this role."
    },
    "name": {
      "type": "String",
      "nullable": false,
      "readonly": true,
      "description": "Name of the user who created this role."
    }
  },
  "lastModified": {
    "type": "String (Datetime)",
    "nullable": false,
    "readonly": true,
    "description": "Timestamp of the last successful update on this role."
  },
  "active": {
    "type": "Boolean",
    "nullable": false,
    "description": "Whether a role is activated or deactivated. A deactivated role does not influence the users privileges when assigned."
  },
  "roleType": {
    "type": "String (Enum)",
    "nullable": false,
    "readonly": true,
    "values": ["PowerUser", "Assistant", "Viewer", "ExternalVisitor", "ExternalCommentator", "SubContractor", "Custom"],
    "description": "The role type of this type."
  },
  "activeProjects": {
    "type": "long",
    "nullable": false,
    "readonly": true,
    "description": "The amount of projects this role is active in."
  }
}
```

## 13. Subscription
```
{
  "type": {
    "type": "String (Enum)",
    "nullable": false,
    "readonly": true,
    "values": ["Master", "Multi", "Combo", "Pocket", "Test"],
    "description": "Type of the subscription."
  },
  "paymentPlan": {
    "type": "String (Enum)",
    "nullable": true,
    "readonly": true,
    "values": ["Annually", "Monthly", "OneTime"],
    "description": "The payment plan of this subscription."
  },
  "paymentType": {
    "type": "String (Enum)",
    "nullable": true,
    "readonly": true,
    "values": ["CreditCard", "DirectDebit", "Other"],
    "description": "The payment type of this subscription."
  },
  "postalBills": {
    "type": "boolean",
    "nullable": false,
    "readonly": true,
    "description": "Whether there are postal bills or not."
  },
  "since": {
    "type": "LocalDate",
    "nullable": false,
    "readonly": true,
    "description": "When the subscription started."
  },
  "until": {
    "type": "LocalDate",
    "nullable": true,
    "readonly": true,
    "description": "When the subscription ends."
  },
  "availableAccounts": {
    "type": "long",
    "nullable": false,
    "readonly": true,
    "description": "The amount of available accounts in this subscription."
  },
  "usedAccounts": {
    "type": "long",
    "nullable": false,
    "readonly": true,
    "description": "The amount of used accounts in this subscription."
  },
  "totalAccounts": {
    "type": "long",
    "nullable": false,
    "readonly": true,
    "description": "The total amount of accounts in this subscription."
  }
}
```