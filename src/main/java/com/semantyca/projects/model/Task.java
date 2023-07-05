package com.semantyca.projects.model;

import com.semantyca.model.Language;
import com.semantyca.model.SecureDataEntity;
import com.semantyca.officeframe.model.Label;
import com.semantyca.officeframe.model.TaskType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class Task extends SecureDataEntity<UUID> {
    private String regNumber;
    private String body;
    protected Long assignee;
    private TaskType taskType;
    private Project project;
    private Task parent;
    protected ZonedDateTime targetDate;
    private ZonedDateTime startDate;
    private int status;
    private Language primaryLang;
    private int priority;
    private int position;
    private String cancellationComment;
    private boolean initiative;
    private List<Label> tags;

    public static class Builder {
        private String body;
        private Language primaryLang = new Language.Builder().build();
        private int status;
        private int position = 999;
        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder setPrimaryLang(Language primaryLang) {
            this.primaryLang = primaryLang;
            return this;
        }

        public Builder setStatus(int status) {
            this.status = status;
            return this;
        }

        public Builder setPosition(int position) {
            this.position = position;
            return this;
        }

        public Task build() {
            Task newNode = new Task();
            newNode.setBody(body);
            newNode.setStatus(status);
            newNode.setPosition(position);
            newNode.setPrimaryLang(primaryLang);
            return newNode;
        }

    }

}
