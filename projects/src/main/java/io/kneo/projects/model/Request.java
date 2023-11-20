package io.kneo.projects.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.model.SecureDataEntity;
import io.kneo.officeframe.model.RequestType;
import io.kneo.projects.model.cnst.ResolutionType;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request extends SecureDataEntity<UUID> {
    private Task task;
    private RequestType requestType;

    private ResolutionType resolution = ResolutionType.UNKNOWN;

    private ZonedDateTime resolutionTime;

    private String decisionComment;


    private String comment;


}
