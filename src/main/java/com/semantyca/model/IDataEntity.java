package com.semantyca.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"entityType", "id", "author", "regDate", "lastModifier", "lastModifiedDate", "title", "new"})
public interface IDataEntity<K> {

    void setId(K id);

    K getId();

    int getAuthor();

    @JsonFormat(pattern="dd.MM.yyyy HH:mm Z")
    ZonedDateTime getRegDate();

    boolean isEditable();

    default String getTitle(){
        return toString();
    }

    void setTitle(String title);

    void setLastModifiedDate(ZonedDateTime lastModifiedDate);

    @JsonFormat(pattern="dd.MM.yyyy HH:mm Z")
    ZonedDateTime getLastModifiedDate();

    void setLastModifier(int last_mod_user);

    void setRegDate(ZonedDateTime reg_date);

    void setAuthor(int author);


}
