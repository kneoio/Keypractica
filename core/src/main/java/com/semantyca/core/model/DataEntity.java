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

    @JsonIgnore
    public Map<String, String> getValuesAsMap() {
        Map<String, String> values = new HashMap<>();
        values.put("author", String.valueOf(author));
        values.put("regDate", String.valueOf(regDate));
        values.put("lastModifiedDate", String.valueOf(lastModifiedDate));
        values.put("lastModifier", String.valueOf(lastModifier));
        return values;
    }
}
