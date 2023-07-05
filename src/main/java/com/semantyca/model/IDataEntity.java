package com.semantyca.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "author", "regDate", "lastModifier", "lastModifiedDate", "title"})
public interface IDataEntity<K> {
    K getId();
    void setAuthor(long author);
    long getAuthor();
    void setRegDate(ZonedDateTime reg_date);
    @JsonFormat(pattern="dd.MM.yyyy HH:mm Z")
    ZonedDateTime getRegDate();
    boolean isEditable();
    default String getTitle(){
        return toString();
    }
    void setLastModifiedDate(ZonedDateTime lastModifiedDate);
    @JsonFormat(pattern="dd.MM.yyyy HH:mm Z")
    ZonedDateTime getLastModifiedDate();
    void setLastModifier(long last_mod_user);
    long getLastModifier();
}
