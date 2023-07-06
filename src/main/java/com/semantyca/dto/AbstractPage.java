package com.semantyca.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.semantyca.dto.cnst.PayloadType;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
public abstract class AbstractPage<T extends AbstractPage> {
    private String title;
    protected  Map<String, Object> payload = new LinkedHashMap<>();

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

    public T addPayload(PayloadType name, Object payload){
        this.payload.put(name.getAlias(), payload);
        return (T) this;
    }

}
