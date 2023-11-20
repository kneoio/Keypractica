package io.kneo.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "author", "regDate", "lastModifier", "lastModifiedDate"})
public interface IDataEntity<K> {
    void setId(K id);
    K getId();
    void setAuthor(long author);
    long getAuthor();
    void setRegDate(ZonedDateTime regDate);
    @JsonFormat(pattern="dd.MM.yyyy HH:mm")
    ZonedDateTime getRegDate();
    boolean isEditable();
    void setLastModifiedDate(ZonedDateTime lastModifiedDate);
    @JsonFormat(pattern="dd.MM.yyyy HH:mm")
    ZonedDateTime getLastModifiedDate();
    void setLastModifier(long lastModifier);
    long getLastModifier();
}
