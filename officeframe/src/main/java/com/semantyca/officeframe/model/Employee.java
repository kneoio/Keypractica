package com.semantyca.officeframe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.semantyca.core.model.SimpleReferenceEntity;
import com.semantyca.core.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.management.relation.Role;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name"})
@Setter
@Getter
@NoArgsConstructor
public class Employee extends SimpleReferenceEntity {
    private String name;
    private User user;
    private Date birthDate;
    private String phone;
    private Organization organization;
    private Department department;
    private Employee boss;
    private Position position;
    private List<Role> roles;
    private int rank = 999;

    public static class Builder {
        private UUID id;
        private String name;
        private Date birthDate;
        private String phone;
        private Organization organization;
        private Department department;
        private Employee boss;
        private Position position;
        private List<Role> roles;
        private int rank = 999;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setBirthDate(Date birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder setOrganization(Organization organization) {
            this.organization = organization;
            return this;
        }

        public Builder setDepartment(Department department) {
            this.department = department;
            return this;
        }

        public Builder setBoss(Employee boss) {
            this.boss = boss;
            return this;
        }

        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        public Builder setRoles(List<Role> roles) {
            this.roles = roles;
            return this;
        }

        public Builder setRank(int rank) {
            this.rank = rank;
            return this;
        }

        public Employee build() {
            Employee newNode = new Employee();
            newNode.setName(name);
            newNode.setId(id);
            newNode.setDepartment(department);
            newNode.setOrganization(organization);
            newNode.setPosition(position);
            newNode.setBoss(boss);
            newNode.setBirthDate(birthDate);
            newNode.setRoles(roles);
            newNode.setPhone(phone);
            newNode.setRank(rank);
            return newNode;
        }
    }
}
