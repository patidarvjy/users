package com.docutools.mfa.sms.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("Verify Code")
public class VerifyCodeDTO {

    @ApiModelProperty("Verification code received by SMS")
    private String code;

    @JsonCreator
    public VerifyCodeDTO(@JsonProperty(required = true, value = "code") String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
