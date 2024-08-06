package io.kneo.projects.dto.filter;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskFilter {
    private String status;
    private String priority;
    private String assignee;
    private String startDate;
    private String endDate;

}

