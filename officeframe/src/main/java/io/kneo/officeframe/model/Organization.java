package io.kneo.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.model.AbstractEntityBuilder;
import io.kneo.core.model.SimpleReferenceEntity;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organization extends SimpleReferenceEntity {
    private OrgCategory orgCategory;
    private List<Department> departments;
    private List<Employee> employers;
    private List<OrganizationLabel> labels;
    private String bizID = "";
    private int rank = 999;
    private boolean isPrimary;

    public static class Builder extends AbstractEntityBuilder {
        private OrgCategory orgCategory;
        private List<Department> departments;
        private List<Employee> employers;
        private List<OrganizationLabel> labels;
        private String bizID = "";
        private int rank = 999;
        private boolean isPrimary;

        public Organization.Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Organization build() {
            Organization doc = new Organization();
            setDefaultFields(doc);

            return doc;
        }
    }
}
