package com.docutools.users.resources

import com.docutools.config.jpa.auditing.AuditedEntity
import com.fasterxml.jackson.annotation.JsonFormat

import java.time.Instant

class AuditedDTO {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    Instant created
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    Instant lastModified

    AuditedDTO() {
    }

    AuditedDTO(AuditedEntity entity) {
        this.created = entity.getCreated()?.toInstant()
        this.lastModified = entity.getLastModified()?.toInstant()
    }
}
