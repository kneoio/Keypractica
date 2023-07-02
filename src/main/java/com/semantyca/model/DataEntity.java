package com.semantyca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.semantyca.util.CustomLocalDateTimeDeserializer;
import com.semantyca.util.CustomLocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public abstract class DataEntity<K> implements IDataEntity<K> {
    protected long author;
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime regDate;
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime lastModifiedDate;
    private long lastModifier;
    private boolean editable;
    private String title = "";

    @JsonIgnore
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
