package com.docutools.config.api


import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Configuration
class JacksonConfig {

    @Bean
    @Primary
    ObjectMapper objectMapper() {
        JavaTimeModule timeModule = new JavaTimeModule()
        timeModule.addSerializer(ZonedDateTime, new ZonedDateTimeSerializer())
        timeModule.addDeserializer(ZonedDateTime, new ZonedDateTimeDeserialzer())

        return new ObjectMapper()
                .findAndRegisterModules()
                .registerModule(timeModule)
    }

    static class ZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime> {

        private static final DateTimeFormatter CUSTOM_ISO_LOCAL_DATE_TIME =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        @Override
        void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            gen.writeString(value.format(CUSTOM_ISO_LOCAL_DATE_TIME))
        }
    }

    static class ZonedDateTimeDeserialzer extends JsonDeserializer<ZonedDateTime> {
        @Override
        ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            ZonedDateTime.parse(p.getValueAsString())
        }
    }
}
