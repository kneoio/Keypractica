package com.semantyca.core.model.embedded;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"readerName", "readingTime", "accessLevel"})
public class RLSEntry {
    public static int NO_ACCESS = 0;
    public static int READ_ONLY = 1;
    public static int EDIT_IS_ALLOWED = 2;
    public static int EDIT_AND_DELETE_ARE_ALLOWED = 3;

    private ZonedDateTime readingTime;
    private long reader;
    private String readerName;
    private int accessLevel = NO_ACCESS;

    @JsonIgnore
    public long getReader() {
        return reader;
    }

    public RLSEntry setReader(long reader) {
        this.reader = reader;
        return this;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public RLSEntry setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
        return this;
    }

    public RLSEntry setReadingTime(ZonedDateTime readingTime) {
        this.readingTime = readingTime;
        return this;

    }

    public RLSEntry setReadRightNow(boolean wasRead) {
        if (wasRead) {
            readingTime = ZonedDateTime.now();
        } else {
            readingTime = null;
        }
        return this;
    }

    @JsonFormat(pattern="dd.MM.yyyy HH:mm")
    public ZonedDateTime getReadingTime() {
        return readingTime;
    }

    public RLSEntry allowEdit() {
        accessLevel = 1;
        return this;
    }


    public RLSEntry revokeEdit() {
        accessLevel = 0;
        return this;
    }

    public String getReaderName() {
        return readerName;
    }

}
