package com.semantyca.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public abstract class DataEntity<K> implements IDataEntity<K> {

    protected K id;

    protected int author;

    private String authorName;

    private ZonedDateTime regDate;

    private ZonedDateTime lastModifiedDate;

    private int lastModifier;

    private String lastModifierName;

    private boolean editable = true;

    private String title = "";

    public void setId(K id) {
        this.id = id;
    }

    public K getId() {
        return id;
    }

    public void setAuthor(int author) {
        this.author = author;
    }

    public String getEntityType() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    @Override
    public int getAuthor() {
        return author;
    }

    @Override
    public ZonedDateTime getRegDate() {
        return regDate;
    }

    public void setRegDate(ZonedDateTime regDate) {
        this.regDate = regDate;
    }

    public void setLastModifiedDate(ZonedDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public ZonedDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }


    public int getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(int lastModifier) {
        this.lastModifier = lastModifier;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    public void setEditable(int isEditable) {
        if (isEditable == 1) {
            editable = true;
        }
        editable = false;
    }

    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        @SuppressWarnings("unchecked")
        DataEntity<UUID> tmp = (DataEntity<UUID>) obj;

        return id != null && id.equals(tmp.id);
    }

}
