package io.kneo.core.dto;

import java.time.ZonedDateTime;

public interface IDTO {
    void setAuthor(String author);
    void setRegDate(ZonedDateTime time);
    void setLastModifier(String lastModifier);
    void setLastModifiedDate(ZonedDateTime modifiedDate);


}
