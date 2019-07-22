package com.docutools.utils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Locale;

/**
 * Converts {@link Locale} to Strings vor JPA entities.
 *
 * @since 1.0
 * @author amp
 */
@Converter(autoApply = true)
public class LocaleConverter implements AttributeConverter<Locale, String> {
    @Override
    public String convertToDatabaseColumn(Locale attribute) {
        return attribute != null? attribute.getLanguage() : null;
    }

    @Override
    public Locale convertToEntityAttribute(String dbData) {
        return dbData != null? new Locale(dbData) : null;
    }
}
