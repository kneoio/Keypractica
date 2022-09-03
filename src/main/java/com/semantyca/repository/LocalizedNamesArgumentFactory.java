package com.semantyca.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semantyca.localization.LanguageCode;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.Map;

public class LocalizedNamesArgumentFactory extends AbstractArgumentFactory<Map<LanguageCode, String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger("LocalizedNamesArgumentFactory");
    private static final ObjectMapper mapper = new ObjectMapper();

    public LocalizedNamesArgumentFactory() {
        super(Types.OTHER);
    }

    @Override
    protected Argument build(Map<LanguageCode, String> names, ConfigRegistry config) {
        return (position, statement, ctx) -> {
            try {
                statement.setObject(position, mapper.writeValueAsString(names), Types.OTHER);
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage());
            }
        };
    }
}
