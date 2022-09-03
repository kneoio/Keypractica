package com.semantyca.repository;

import com.semantyca.model.IDataEntity;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

public abstract class AbstractMapper<T> implements ColumnMapper<T> {

    @Override
    public abstract T map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException;


    @Override
    public T map(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException{
        return null;
    }


    public static ZonedDateTime getDateTime(Timestamp timestamp) {
        return timestamp != null ? ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp.getTime()), ZoneOffset.UTC) : null;
    }

    public static void transferIdInteger(IDataEntity entity, ResultSet rs) throws SQLException {
        entity.setId(rs.getInt("id"));
    }

    public static void transferIdUUID(IDataEntity entity, ResultSet rs) throws SQLException {
        entity.setId(rs.getObject("id", UUID.class));

    }

    public static void transferCommonData(IDataEntity entity, ResultSet rs) throws SQLException {
        entity.setLastModifiedDate(getDateTime(rs.getTimestamp("last_mod_date")));
        entity.setLastModifier(rs.getInt("last_mod_user"));
        entity.setRegDate(getDateTime(rs.getTimestamp("reg_date")));
        entity.setTitle(rs.getString("title"));
        entity.setAuthor(rs.getInt("author"));
    }


}
