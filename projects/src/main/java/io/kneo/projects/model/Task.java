package io.kneo.projects.model;

import io.kneo.core.model.AbstractEntityBuilder;
import io.kneo.core.model.Label;
import io.kneo.core.model.SecureDataEntity;
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
    private String title;
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

    public static class Builder extends AbstractEntityBuilder {
        private String regNumber;
        private String title;
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

        public Builder setTitle(String title) {
            this.title = title;
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
            Task doc = new Task();
            setDefaultFields(doc);
            doc.setRegNumber(regNumber);
            doc.setTitle(title);
            doc.setBody(body);
            doc.setAssignee(assignee);
            doc.setTaskType(taskType);
            doc.setProject(project);
            doc.setParent(parent);
            doc.setTargetDate(targetDate);
            doc.setStartDate(startDate);
            doc.setStatus(status);
            doc.setPriority(priority);
            doc.setCancellationComment(cancellationComment);
            doc.setInitiative(initiative);
            doc.setTags(tags);
            return doc;
        }

    }

}
