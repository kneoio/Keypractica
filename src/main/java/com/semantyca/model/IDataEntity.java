package com.semantyca.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "author", "regDate", "lastModifier", "lastModifiedDate", "title"})
public interface IDataEntity<K> {
    K getIdentifier();
    void setAuthor(long author);
    long getAuthor();
    void setRegDate(LocalDateTime reg_date);
    @JsonFormat(pattern="dd.MM.yyyy HH:mm Z")
    LocalDateTime getRegDate();
    boolean isEditable();
    default String getTitle(){
        return toString();
    }
    void setLastModifiedDate(LocalDateTime lastModifiedDate);
    @JsonFormat(pattern="dd.MM.yyyy HH:mm Z")
    LocalDateTime getLastModifiedDate();
    void setLastModifier(long last_mod_user);
    long getLastModifier();
}
