package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.model.SimpleReferenceEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskType extends SimpleReferenceEntity {

    public String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


}
