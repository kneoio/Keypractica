package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SimpleReferenceEntity;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organization extends SimpleReferenceEntity {
    private String name;
    private OrgCategory orgCategory;
    private List<Department> departments;
    private List<Employee> employers;
    private List<OrganizationLabel> labels;
    private String bizID = "";
    private int rank = 999;
    private boolean isPrimary;

    public OrgCategory getOrgCategory() {
        return orgCategory;
    }

    public void setOrgCategory(OrgCategory orgCategory) {
        this.orgCategory = orgCategory;
    }

    @JsonIgnore
    public List<Department> getDepartments() {
        return departments;
    }

    @JsonIgnore
    public List<Employee> getEmployers() {
        return employers;
    }

    public void setEmployers(List<Employee> employers) {
        this.employers = employers;
    }

    public String getBizID() {
        return bizID;
    }

    public void setBizID(String bizID) {
        this.bizID = bizID;
    }

    public List<OrganizationLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<OrganizationLabel> labels) {
        this.labels = labels;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }


    public static class Builder {
        public Organization build() {
            return new Organization();
        }
    }
}
