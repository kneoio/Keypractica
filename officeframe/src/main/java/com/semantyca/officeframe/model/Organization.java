package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.AbstractEntityBuilder;
import com.semantyca.core.model.SimpleReferenceEntity;

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
