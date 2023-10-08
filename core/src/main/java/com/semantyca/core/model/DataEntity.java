package com.semantyca.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public abstract class DataEntity<K> implements IDataEntity<K> {
    private K id;
    private long author;
    private ZonedDateTime regDate;
    private ZonedDateTime lastModifiedDate;
    private long lastModifier;

    @Override
    public K getId() {
        return id;
    }

    @Override
    public void setId(K id) {
        this.id = id;
    }

    @Override
    public long getAuthor() {
        return author;
    }

    @Override
    public void setAuthor(long author) {
        this.author = author;
    }

    @Override
    public ZonedDateTime getRegDate() {
        return regDate;
    }

    @Override
    public void  setRegDate(ZonedDateTime regDate) {
        this.regDate = regDate;
    }

    @Override
    public ZonedDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public void setLastModifiedDate(ZonedDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public long getLastModifier() {
        return lastModifier;
    }

    @Override
    public void setLastModifier(long lastModifier) {
        this.lastModifier = lastModifier;
    }

    @JsonIgnore
    public Map<String, String> getValuesAsMap() {
        Map<String, String> values = new HashMap<>();
        values.put("author", String.valueOf(author));
        values.put("regDate", String.valueOf(regDate));
        values.put("lastModifiedDate", String.valueOf(lastModifiedDate));
        values.put("lastModifier", String.valueOf(lastModifier));
        return values;
    }

    @Override
    public boolean isEditable() {
        return true;
    }
}
