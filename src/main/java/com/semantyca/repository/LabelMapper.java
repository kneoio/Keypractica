package com.semantyca.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semantyca.localization.LanguageCode;
import com.semantyca.repository.glossary.Label;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class LabelMapper extends AbstractMapper<Label> {
    private static ObjectMapper mapper = new ObjectMapper();
    private static  JavaType locNamesMapType = mapper.getTypeFactory().constructMapType(HashMap.class, LanguageCode.class, String.class);

    public LabelMapper() {
        super();
    }

    @Override
    public Label map(ResultSet rs, int columnNumber, StatementContext ctx) throws SQLException {
        Label entity = new Label();
        transferIdUUID(entity, rs);
        transferCommonData(entity, rs);
        entity.setName(rs.getString("name"));
        entity.setColor(rs.getString("color"));
        entity.setCategory(rs.getString("category"));
        entity.setActive(rs.getBoolean("is_active"));
        entity.setRank(rs.getInt("rank"));
        try {
            entity.setLocalizedNames(mapper.readValue(rs.getObject("localized_names", PGobject.class).getValue(), locNamesMapType));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            entity.setLocalizedNames(new HashMap<>());
        }
        return entity;
    }
}
