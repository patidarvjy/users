package com.docutools.exceptions;

public enum ErrorCodes {

    WEAK_PASSWORD(1),
    INCORRECT_FILETYPE(2),
    MISSING_REQUIRED_VALUE(3),
    UNPRIVILEGED(4),
    FILE_NOT_FOUND(5),
    FILE_PARSING(6),
    USER_NOT_FOUND(7),
    INCORRECT_PASSWORD(8),
    ENDPOINT_DISABLED(9),
    CONTACT_NOT_SAME_PROJECT(10),
    CONFLICT(11),
    FORBIDDEN(12),
    INTERNAL_SERVER_ERROR(13),
    RESOURCE_NOT_FOUND(14),
    DIFFERENT_ORGANISATION(15),
    INVALID_RESOURCE(16),
    EMAIL_BLOCKED(17),
    VERIFICATION_NOT_REQUIRED(18),
    EXPIRED_RESOURCE(19),
    CONFLICT_WITH_MESSAGE(20),
    SAML_CANT_CHANGE_EMAIL(21),
    SAML_CANT_CHANGE_PASSWORD(22),
    NO_LICENSES(23),
    RESOURCE_IN_USE(24),
    COULD_NOT_PARSE_TIMESTAMP(25),
    SIGNITURE_VALIDATION_ERROR(26),
    UNAUTHORIZED(27),
    EMAIL_IN_USE(28),
    EMAIL_ALREADY_VERIFIED(29),
    MODIFY_AVATAR_FOR_UNKNOWN_USER(30),
    INVALID_VERIFICATION_CODE(31);

    private final int code;

    ErrorCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}