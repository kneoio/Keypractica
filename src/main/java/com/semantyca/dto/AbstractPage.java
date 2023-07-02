package com.semantyca.dto;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractPage<T extends AbstractPage> {
    protected  Map<String, Object> payload = new LinkedHashMap();

    @JsonIgnore
    public T addPayload(Object payload){
        this.payload.put(payload.getClass().getSimpleName().toLowerCase(), payload);
        return (T) this;
    }

    @JsonIgnore
    public T addPayload(String name, Object payload){
        this.payload.put(name.toLowerCase(), payload);
        return (T) this;
    }
    @JsonGetter("payload")
    public Map<String, Object> getPayload() {
        return payload;
    }

}
