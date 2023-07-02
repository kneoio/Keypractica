package com.semantyca.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public abstract class DataEntity<K> implements IDataEntity<K> {
    protected long author;
    private LocalDateTime regDate;
    private LocalDateTime lastModifiedDate;
    private long lastModifier;
    private boolean editable;
    private String title = "";

    public Map<String, String> getValuesAsMap() {
        Map<String, String> values = new HashMap<>();
        values.put("author", String.valueOf(author));
        values.put("regDate", String.valueOf(regDate));
        values.put("lastModifiedDate", String.valueOf(lastModifiedDate));
        values.put("lastModifier", String.valueOf(lastModifier));
        values.put("title", title);
        return values;
    }
}
