package io.kneo.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public LocalDateTime deserialize(JsonParser jsonparser, DeserializationContext context) throws IOException {
        String date = jsonparser.getText();
        return LocalDateTime.parse(date, formatter);
    }

}
