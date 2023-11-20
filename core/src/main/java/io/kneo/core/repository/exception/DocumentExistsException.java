package io.kneo.core.repository.exception;


public class DocumentExistsException extends Exception {
    private String  docId;

    public DocumentExistsException(String id) {
        super();
        docId = id;
    }

    public String getMessage() {
        return "{\"error\":\"" + String.format("The document '%s' is exists.", docId) + "\"}";
    }

    public String getDetailedMessage() {
        return String.format("DocumentExistsException: %s is exists", docId.toString());
    }
}
