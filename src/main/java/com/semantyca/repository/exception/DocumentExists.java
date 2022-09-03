package com.semantyca.repository.exception;


public class DocumentExists extends Exception {
    private String  docId;

    public DocumentExists(String id) {
        super();
        docId = id;
    }

    public String getDeveloperMessage() {
        return docId.toString() + " not found";
    }
}
