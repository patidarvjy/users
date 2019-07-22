package com.docutools.exceptions;

import com.docutools.apierrors.ApiException;

import java.util.UUID;

import static com.docutools.apierrors.ApiErrors.apiError;
import static com.docutools.exceptions.ErrorCodes.*;

public class ExceptionHelper {

    public static ApiException newUnprivilegedError(String value) {
        return apiError().forbidden().code(UNPRIVILEGED.getCode()).info("value", value).getApiException();
    }

    public static ApiException newMissingRequiredValueError(String value) {
        return apiError().badRequest().code(MISSING_REQUIRED_VALUE.getCode()).info("value", value).getApiException();
    }

    public static ApiException newConflictError() {
        return apiError().conflict().code(CONFLICT.getCode()).getApiException();
    }

    public static ApiException newConflictError(String value) {
        return apiError().conflict().code(CONFLICT_WITH_MESSAGE.getCode()).info("value", value).getApiException();
    }

    public static ApiException newForbiddenError() {
        return apiError()
                .forbidden()
                .code(FORBIDDEN.getCode())
                .getApiException();
    }

    public static ApiException newForbiddenError(String value) {
        return apiError()
                .forbidden()
                .code(FORBIDDEN.getCode())
                .info("value", value)
                .getApiException();
    }

    public static ApiException newInputValidationError(String value) {
        return apiError()
                .badRequest()
                .code(INVALID_RESOURCE.getCode())
                .info("value", value)
                .getApiException();
    }

    public static ApiException newUnauthorizedError() {
        return apiError()
                .unauthorized()
                .code(UNAUTHORIZED.getCode())
                .getApiException();
    }

    public static ApiException newUnauthorizedError(String value) {
        return apiError()
                .unauthorized()
                .code(UNAUTHORIZED.getCode())
                .info("value", value)
                .getApiException();
    }

    public static ApiException newInternalServerError(String reason, Throwable cause) {
        return apiError()
                .internalServerError()
                .code(INTERNAL_SERVER_ERROR.getCode())
                .cause(cause)
                .customMessage(reason)
                .getApiException();
    }

    public static ApiException newInternalServerError(String reason) {
        return apiError()
                .internalServerError()
                .code(INTERNAL_SERVER_ERROR.getCode())
                .customMessage(reason)
                .getApiException();
    }

    public static ApiException newBadRequestError(ErrorCodes errorCode, Throwable e) {
        return apiError()
                .code(errorCode.getCode())
                .cause(e)
                .badRequest()
                .getApiException();
    }

    public static ApiException newBadRequestError(ErrorCodes errorCode, String value, Throwable cause) {
        return apiError()
                .code(errorCode.getCode())
                .cause(cause)
                .info("value", value)
                .badRequest()
                .getApiException();
    }

    public static ApiException newBadRequestError(ErrorCodes errorCode, String value) {
        return apiError()
                .code(errorCode.getCode())
                .info("value", value)
                .badRequest()
                .getApiException();
    }

    public static ApiException newBadRequestError(ErrorCodes errorCode, String value, String value2) {
        return apiError()
                .code(errorCode.getCode())
                .info("value", value)
                .info("value2", value2)
                .badRequest()
                .getApiException();
    }

    public static ApiException newBadRequestError(ErrorCodes errorCode) {
        return apiError()
                .code(errorCode.getCode())
                .badRequest()
                .getApiException();
    }

    public static ApiException newResourceNotFoundError(String value) {
        return apiError()
                .code(RESOURCE_NOT_FOUND.getCode())
                .info("value", value)
                .notFound()
                .getApiException();
    }

    public static ApiException newResourceNotFoundError(String value, UUID id) {
        return apiError()
                .code(RESOURCE_NOT_FOUND.getCode())
                .info("value", String.format("%s %s ",value, id))
                .notFound()
                .getApiException();
    }
}
