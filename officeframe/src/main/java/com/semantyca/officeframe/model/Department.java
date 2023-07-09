package com.semantyca.officeframe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SimpleReferenceEntity;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class Department extends SimpleReferenceEntity {

    private DepartmentType type;

    private Organization organization;


    private Department leadDepartment;


    private Employee boss;

    private int rank = 999;

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Department getLeadDepartment() {
        return leadDepartment;
    }

    public void setLeadDepartment(Department leadDepartment) {
        this.leadDepartment = leadDepartment;
    }

    public Employee getBoss() {
        return boss;
    }

    public void setBoss(Employee boss) {
        this.boss = boss;
    }

    public DepartmentType getType() {
        return type;
    }

    public void setType(DepartmentType type) {
        this.type = type;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

}
