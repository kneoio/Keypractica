package com.semantyca.projects.model;

import com.semantyca.core.model.AbstractEntityBuilder;
import com.semantyca.core.model.Language;
import com.semantyca.core.model.SecureDataEntity;
import com.semantyca.core.model.constants.ProjectStatusType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class Project extends SecureDataEntity<UUID> {
    protected String name;
    private long manager;
    private long coder;
    private long tester;
    private ProjectStatusType status;
    private Language primaryLang;
    private int position;
    private LocalDate finishDate;
    private String comment;

    public static class Builder extends AbstractEntityBuilder {
        private String name;
        private int manager;
        private int coder;
        private int tester;
        private Language primaryLang = new Language.Builder().build();
        private LocalDate finishDate;
        private ProjectStatusType status = ProjectStatusType.DRAFT;
        private int position = 999;
        private String comment;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setManager(int manager) {
            this.manager = manager;
            return this;
        }

        public Builder setCoder(int coder) {
            this.coder = coder;
            return this;
        }

        public Builder setTester(int tester) {
            this.tester = tester;
            return this;
        }

        public Builder setFinishDate(LocalDate finishdate) {
            this.finishDate = finishdate;
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

        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Project build() {
            Project doc = new Project();
            setDefaultFields(doc);
            doc.setName(name);
            doc.setManager(manager);
            doc.setCoder(coder);
            doc.setTester(tester);
            doc.setFinishDate(finishDate);
            doc.setStatus(status);
            doc.setPosition(position);
            doc.setPrimaryLang(primaryLang);
            doc.setComment(comment);
            return doc;
        }



    }

}
