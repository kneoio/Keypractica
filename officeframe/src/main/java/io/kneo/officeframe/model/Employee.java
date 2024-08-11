package io.kneo.officeframe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.model.SimpleReferenceEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class Employee extends SimpleReferenceEntity {
    private long userId;
    private LocalDate birthDate;
    private String phone;
    private UUID organization;
    private UUID department;
    private UUID position;
    private List<String> roles;
    private int rank = 999;
    private int status;
}

