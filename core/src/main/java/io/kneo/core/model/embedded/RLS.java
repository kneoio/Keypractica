package io.kneo.core.model.embedded;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.kneo.core.dto.cnst.AccessLevel;
import lombok.Getter;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"reader", "readingTime", "canEdit", "canDelete"})
public class RLS {
    @Getter
    private ZonedDateTime readingTime;
    @Getter
    private final long reader;
    private final long canEdit;
    private final long canDelete;

    public RLS(ZonedDateTime readingTime, long reader, long canEdit, long canDelete) {
        this.readingTime = readingTime;
        this.reader = reader;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

    public AccessLevel getAccessLevel() {
        if (canEdit == 1) {
            if (canDelete == 1) {
                return AccessLevel.EDIT_AND_DELETE_ARE_ALLOWED;
            }
            return AccessLevel.EDIT_IS_ALLOWED;
        }
       return AccessLevel.READ_ONLY;
    }

    public RLS setRead(boolean wasRead) {
        if (wasRead) {
            readingTime = ZonedDateTime.now();
        } else {
            readingTime = null;
        }
        return this;
    }


}
