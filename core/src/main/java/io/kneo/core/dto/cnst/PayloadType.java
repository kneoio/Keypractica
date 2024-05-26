package io.kneo.core.dto.cnst;

import lombok.Getter;

@Getter
public enum PayloadType {
    CONTEXT_ACTIONS("actions"),
    EXCEPTION("exception"),
    TEXT("text"),
    VIEW_DATA("viewData"),
    DOC_DATA("docData"),
    TEMPLATE("template");

    private final String alias;

    PayloadType(String alias) {
        this.alias = alias;
    }

}
