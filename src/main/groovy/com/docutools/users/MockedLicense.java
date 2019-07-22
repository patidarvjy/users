package com.docutools.users;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@ApiModel(value = "License Resource")
public class MockedLicense {

    @ApiModelProperty(value = "Type of the License")
    private String type;
    @JsonFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            timezone = "UTC"
    )
    @ApiModelProperty(value = "Timestamp when the License Started")
    private Instant since = LocalDateTime.now().minusMonths(1).toInstant(ZoneOffset.UTC);
    @JsonFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            timezone = "UTC"
    )
    @ApiModelProperty(value = "Timestamp when the License ends")
    private Instant until;
    @ApiModelProperty(value = "Whether the License is paid or not")
    private boolean paid;

    public MockedLicense(String type, Instant until,Instant since) {
        if(type != null)
            this.type = type;
        else
            this.type = "Test";
        if(until != null)
            this.until = until;
        paid = type != null && !type.equals("Test") && !type.equals("None");
        if (since != null) this.since = since;
    }

    public String getType() {
        return type;
    }

    public Instant getSince() {
        return since;
    }

    public Instant getUntil() {
        return until;
    }

    public boolean isPaid() {
        return paid;
    }
}
