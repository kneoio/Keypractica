package com.semantyca.projects.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SecureDataEntity;
import com.semantyca.officeframe.model.RequestType;
import com.semantyca.projects.model.cnst.ResolutionType;

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
