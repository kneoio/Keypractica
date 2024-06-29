package io.kneo.projects.model;

import io.kneo.core.localization.LanguageCode;
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
    private LocalDate startDate;
    private LocalDate finishDate;
    private String description;
}
