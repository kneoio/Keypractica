package io.kneo.projects.model;

import io.kneo.core.model.SecureDataEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class Task extends SecureDataEntity<UUID> {
    private String regNumber;
    private String title;
    private String body;
    private Long assignee;
    private UUID taskType;
    private UUID project;
    private UUID parent;
    private LocalDate targetDate;
    private LocalDate startDate;
    private int status;
    private int priority;
    private String cancellationComment;
    private List<UUID> labels;

}
