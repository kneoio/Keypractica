package com.semantyca.officeframe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SimpleReferenceEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@JsonInclude(JsonInclude.Include.NON_NULL)@Setter
@Getter
@NoArgsConstructor
public class Department extends SimpleReferenceEntity {
    private DepartmentType type;
    private Organization organization;
    private Department leadDepartment;
    private Employee boss;
    private int rank = 999;
}
