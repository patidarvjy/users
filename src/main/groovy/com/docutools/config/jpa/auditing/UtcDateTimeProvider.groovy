package com.docutools.config.jpa.auditing

import org.springframework.data.auditing.DateTimeProvider

import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Implementation of Spring's {@link org.springframework.data.auditing.DateTimeProvider} interface giving a {@link GregorianCalendar} instance with the
 * current UTC time.
 */
class UtcDateTimeProvider implements DateTimeProvider {

    @Override
    Optional<ZonedDateTime> getNow() {
        Optional.of(ZonedDateTime.now(ZoneId.of('Z')))
    }

}