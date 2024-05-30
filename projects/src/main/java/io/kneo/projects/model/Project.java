package io.kneo.projects.model;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.AbstractEntityBuilder;
import io.kneo.core.model.SecureDataEntity;
import io.kneo.projects.model.cnst.ProjectStatusType;
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
    private LanguageCode primaryLang;
    private int position;
    private LocalDate finishDate;
    private String description;

    public static class Builder extends AbstractEntityBuilder {
        private String name;
        private long manager;
        private long coder;
        private long tester;
        private LanguageCode primaryLang = LanguageCode.ENG;
        private LocalDate finishDate;
        private ProjectStatusType status = ProjectStatusType.DRAFT;
        private String descr;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setManager(long manager) {
            this.manager = manager;
            return this;
        }

        public Builder setCoder(long coder) {
            this.coder = coder;
            return this;
        }

        public Builder setTester(long tester) {
            this.tester = tester;
            return this;
        }

        public Builder setFinishDate(LocalDate finishdate) {
            this.finishDate = finishdate;
            return this;
        }

        public Builder setPrimaryLang(LanguageCode primaryLang) {
            this.primaryLang = primaryLang;
            return this;
        }

        public Builder setStatus(ProjectStatusType status) {
            this.status = status;
            return this;
        }

        public Builder setDescription(String descr) {
            this.descr = descr;
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
            doc.setPrimaryLang(primaryLang);
            doc.setDescription(descr);
            return doc;
        }



    }

}
