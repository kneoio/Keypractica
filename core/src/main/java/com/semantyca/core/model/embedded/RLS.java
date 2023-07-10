package com.semantyca.core.model.embedded;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"reader", "readingTime", "canEdit", "canDelete"})
public class RLS {
    public static int NO_ACCESS = 0;
    public static int READ_ONLY = 1;
    public static int EDIT_IS_ALLOWED = 2;
    public static int EDIT_AND_DELETE_ARE_ALLOWED = 3;
    private int accessLevel = NO_ACCESS;
    private ZonedDateTime readingTime;
    private long reader;
    private long canEdit;
    private long canDelete;

    public RLS(ZonedDateTime readingTime, long reader, long canEdit, long canDelete) {
        this.readingTime = readingTime;
        this.reader = reader;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

    public long getCanEdit() {
        return canEdit;
    }

    public long getCanDelete() {
        return canDelete;
    }

    public long getReader() {
        return reader;
    }

    public RLS setReader(long reader) {
        this.reader = reader;
        return this;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public RLS setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
        return this;
    }

    public RLS setReadingTime(ZonedDateTime readingTime) {
        this.readingTime = readingTime;
        return this;

    }

    public RLS setRead(boolean wasRead) {
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


  }
