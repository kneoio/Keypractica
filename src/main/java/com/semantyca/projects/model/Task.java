package com.semantyca.projects.model;

import com.semantyca.model.DataEntity;
import com.semantyca.model.Language;
import com.semantyca.projects.model.constants.ProjectStatusType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@Setter
@Getter
@NoArgsConstructor
@NodeEntity
public class Task extends DataEntity<String> {
    @Id
    private String identifier;
    private String body;
    protected Long assignee;
    private ProjectStatusType status;
    private Language primaryLang;
    private int priority;
    private int position;

    public static class Builder {
        private String body;
        private Language primaryLang = new Language.Builder().build();
        private ProjectStatusType status = ProjectStatusType.DRAFT;
        private int position = 999;
        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder setPrimaryLang(Language primaryLang) {
            this.primaryLang = primaryLang;
            return this;
        }

        public Builder setStatus(ProjectStatusType status) {
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
