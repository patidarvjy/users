package com.docutools.users.messages;

import com.docutools.users.values.CsvHeaderColumn;
import com.docutools.users.values.LanguageTag;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class LanguagesLoader {

    private static final String languageFilePath = "/definitions/translations/%s.json";
    private Map<String, Map<String, String>> languageMap = new HashMap<>();

    public LanguagesLoader() {
        for (LanguageTag language : LanguageTag.getLanguages()) {
            Resource languageResource = new ClassPathResource(String.format(languageFilePath, language.toString().toLowerCase()));
            try (InputStream in = languageResource.getInputStream()) {
                languageMap.put(language.toString().toLowerCase(), new Jackson2ObjectMapperBuilder().build().readValue(in, Map.class));
            } catch (IOException e) {
                throw new IllegalStateException("Can not read labels from json file!");
            }
        }
    }

    public String getLocalisedLabel(CsvHeaderColumn csvHeaderColumn, String lang) {
        return languageMap.get(lang).getOrDefault(csvHeaderColumn.getValue(), "en");
    }
}