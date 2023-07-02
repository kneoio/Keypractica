package com.semantyca.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.semantyca.util.CustomLocalDateTimeDeserializer;
import com.semantyca.util.CustomLocalDateTimeSerializer;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;


@Singleton
public class MapperCustomizer implements ObjectMapperCustomizer {

//TODO make it able to register (to eliminate the field annotation)

    @Override
    public void customize(ObjectMapper objectMapper) {
        System.out.println("##################################################################regiater");
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        module.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
        objectMapper.registerModule(module);
    }
}
