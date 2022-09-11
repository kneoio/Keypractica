package com.semantyca.repository.exception;


public class DocumentExistsException extends Exception {
    private String  docId;

    public DocumentExistsException(String id) {
        super();
        docId = id;
    }

    public String getMessage() {
        return "The document is exists";
    }

    public String getDeveloperMessage() {
        return docId.toString() + " is exists";
    }
}
