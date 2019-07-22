package com.docutools.utils;

import com.docutools.exceptions.ExceptionHelper;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static com.docutools.exceptions.ErrorCodes.COULD_NOT_PARSE_TIMESTAMP;

public class DateUtils {

    private DateUtils() {
    }


    public static Instant parseDateTime(String since) {
        Instant sinceDateTime = null;
        if (!StringUtils.isEmpty(since)) {
            try {
                sinceDateTime = Instant.parse(since);
            } catch (DateTimeParseException e) {
                throw ExceptionHelper.newBadRequestError(COULD_NOT_PARSE_TIMESTAMP, since);
            }
        }
        return sinceDateTime;
    }
}
