package io.kneo.core.repository.exception;


import java.util.UUID;

public class DocumentHasNotFoundException extends RuntimeException  {


    public DocumentHasNotFoundException(String id) {
        super(id);
    }

    public DocumentHasNotFoundException(UUID id) {
        super(id.toString());
    }


}
