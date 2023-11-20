package io.kneo.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.kneo.core.dto.cnst.PayloadType;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
public abstract class AbstractPage {
    private String title;
    protected  Map<String, Object> payload = new LinkedHashMap<>();

    @JsonIgnore
    public void addPayload(Object payload){
        this.payload.put(payload.getClass().getSimpleName().toLowerCase(), payload);
    }

    @JsonIgnore
    public void addPayload(String name, Object payload){
        this.payload.put(name.toLowerCase(), payload);
    }

    public void addPayload(PayloadType name, Object payload){
        this.payload.put(name.getAlias(), payload);
    }

}
