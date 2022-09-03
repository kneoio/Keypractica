package com.semantyca.dto;


import com.semantyca.dto.constant.MessageLevel;

public class FeedbackEntry {
    private String id;
    private MessageLevel level;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MessageLevel getLevel() {
        return level;
    }

    public void setLevel(MessageLevel level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
