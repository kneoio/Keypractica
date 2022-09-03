package com.semantyca.repository;

import com.semantyca.model.phrase.Phrase;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PhraseMapper extends AbstractMapper<Phrase> {


    public PhraseMapper() {
        super();
    }

    @Override
    public Phrase map(ResultSet rs, int columnNumber, StatementContext ctx) throws SQLException {
        Phrase entity = new Phrase();
        transferIdUUID(entity, rs);
        transferCommonData(entity, rs);
        entity.setBase(rs.getString("base"));
        entity.setBasePronunciation(rs.getString("base_pronunciation"));
        entity.setTranslation(rs.getString("translation"));
        entity.setTranslationPronunciation(rs.getString("translation_pronunciation"));
        return entity;
    }
}
