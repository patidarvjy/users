package com.docutools.utils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.ZoneId;

/**
 * Converts {@link ZoneId}s to Strings and vice versa for JPA entities.
 *
 * @since 1.0
 * @author amp
 */
@Converter(autoApply = true)
public class ZoneIdJpaConverter implements AttributeConverter<ZoneId, String> {
    @Override
    public String convertToDatabaseColumn(ZoneId attribute) {
        return attribute != null? attribute.getId() : null;
    }

    @Override
    public ZoneId convertToEntityAttribute(String dbData) {
        return dbData != null? ZoneId.of(dbData) : null;
    }
}
