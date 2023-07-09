package com.semantyca.core.repository.exception;

import java.util.UUID;


public class DocumentModificationAccessException extends Exception {
    private UUID docId;

    public DocumentModificationAccessException(UUID id) {
        super();
        docId = id;
    }

    public String getDeveloperMessage() {
        return "\"" + docId.toString() + "\" restricted to modify";
    }
}
