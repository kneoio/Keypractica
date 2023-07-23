package com.semantyca.projects.model;

import com.semantyca.core.model.SecureDataEntity;
import com.semantyca.officeframe.model.Label;
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
    private Long assignee;
    private UUID taskType;
    private UUID project;
    private UUID parent;
    private ZonedDateTime targetDate;
    private ZonedDateTime startDate;
    private int status;
    private int priority;
    private String cancellationComment;
    private boolean initiative;
    private List<Label> tags;

    public static class Builder {
        private UUID id;
        private long author;
        private ZonedDateTime regDate;
        private ZonedDateTime lastModifiedDate;
        private long lastModifier;
        private String regNumber;
        private String body;
        protected Long assignee;
        private UUID taskType;
        private UUID project;
        private UUID parent;
        protected ZonedDateTime targetDate;
        private ZonedDateTime startDate;
        private int status;
        private int priority;
        private String cancellationComment;
        private boolean initiative;
        private List<Label> tags;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setAuthor(long author) {
            this.author = author;
            return this;
        }

        public Builder setRegDate(ZonedDateTime regDate) {
            this.regDate = regDate;
            return this;
        }

        public Builder setLastModifiedDate(ZonedDateTime lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
            return this;
        }

        public Builder setLastModifier(long lastModifier) {
            this.lastModifier = lastModifier;
            return this;
        }

        public Builder setRegNumber(String regNumber) {
            this.regNumber = regNumber;
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder setAssignee(Long assignee) {
            this.assignee = assignee;
            return this;
        }

        public Builder setTaskType(UUID taskType) {
            this.taskType = taskType;
            return this;
        }

        public Builder setProject(UUID project) {
            this.project = project;
            return this;
        }

        public Builder setParent(UUID parent) {
            this.parent = parent;
            return this;
        }

        public Builder setTargetDate(ZonedDateTime targetDate) {
            this.targetDate = targetDate;
            return this;
        }

        public Builder setStartDate(ZonedDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder setStatus(int status) {
            this.status = status;
            return this;
        }

        public Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }
        public Builder setCancellationComment(String cancellationComment) {
            this.cancellationComment = cancellationComment;
            return this;
        }

        public Builder setInitiative(boolean initiative) {
            this.initiative = initiative;
            return this;
        }

        public Builder setTags(List<Label> tags) {
            this.tags = tags;
            return this;
        }

        public Task build() {
            Task newNode = new Task();
            newNode.setId(id);
            newNode.setAuthor(author);
            newNode.setRegDate(regDate);
            newNode.setLastModifier(lastModifier);
            newNode.setLastModifiedDate(lastModifiedDate);
            newNode.setRegNumber(regNumber);
            newNode.setBody(body);
            newNode.setAssignee(assignee);
            newNode.setTaskType(taskType);
            newNode.setProject(project);
            newNode.setParent(parent);
            newNode.setTargetDate(targetDate);
            newNode.setStartDate(startDate);
            newNode.setStatus(status);
            newNode.setPriority(priority);
            newNode.setCancellationComment(cancellationComment);
            newNode.setInitiative(initiative);
            newNode.setTags(tags);
            return newNode;
        }

    }

}
