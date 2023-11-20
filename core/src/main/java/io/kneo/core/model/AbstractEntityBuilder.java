package io.kneo.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.UUID;


public class AbstractEntityBuilder {
    protected static final Logger LOGGER = LoggerFactory.getLogger("AbstractEntityBuilder");

    protected UUID id;
    protected long author;
    protected ZonedDateTime regDate;
    protected ZonedDateTime lastModifiedDate;
    protected long lastModifier;

    public void setDefaultFields(IDataEntity<UUID> entity) {
        entity.setId(id);
        entity.setAuthor(author);
        entity.setRegDate(regDate);
        entity.setLastModifiedDate(lastModifiedDate);
        entity.setLastModifier(lastModifier);
    }



}
