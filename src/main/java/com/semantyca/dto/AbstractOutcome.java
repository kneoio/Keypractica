package com.semantyca.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.ws.rs.core.Link;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractOutcome<T extends AbstractOutcome> {
    protected  Map<String, Object> payloads = new LinkedHashMap();
    protected List<Link> links = new ArrayList<>();
    @JsonIgnore
    public T addPayload(Object payload){
        payloads.put(payload.getClass().getSimpleName().toLowerCase(), payload);
        return (T) this;
    }

}
