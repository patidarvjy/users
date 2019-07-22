package com.docutools.utils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Created by amp on 26.06.17.
 */
@Converter(autoApply = true)
public class ZonedDateTimeJpaConverter implements AttributeConverter<ZonedDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime attribute) {
        if(attribute != null) {
            return Timestamp.from(attribute.toInstant());
        } else {
            return null;
        }
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp dbData) {
        if(dbData != null) {
            return dbData.toInstant().atZone(ZoneId.of("UTC"));
        } else {
            return null;
        }
    }
}
