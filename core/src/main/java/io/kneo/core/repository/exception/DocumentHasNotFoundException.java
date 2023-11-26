package io.kneo.core.repository.exception;


public class DocumentHasNotFoundException extends RuntimeException  {
    private final String  docId;

    public DocumentHasNotFoundException(String id) {
        super();
        docId = id;
    }

    public String getMessage() {
        return "{\"error\":\"" + String.format("The document '%s' has not been found.", docId) + "\"}";
    }

    public String getDetailedMessage() {
        return String.format("The document '%s' has not been found.", docId);
    }


}
