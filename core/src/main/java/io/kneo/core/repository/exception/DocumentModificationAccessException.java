package io.kneo.core.repository.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class DocumentModificationAccessException extends Exception {
    private final String user;
    private String docId;

    public DocumentModificationAccessException(String s, String user, UUID id) {
        super(s);
        this.user = user;
        try {
            this.docId = id.toString();
        } catch (Exception e) {
            docId = "null";
        }
    }

    public String getDeveloperMessage() {
        return String.format(" %s, id: %s ", getMessage(), docId);
    }
}
