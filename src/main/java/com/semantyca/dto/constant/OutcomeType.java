package com.semantyca.dto.constant;

public enum OutcomeType {
    //default
    UNKNOWN,
    //entity payload
    VIEW_PAGE, STATIC_PAGE, DOCUMENT,
    //process response payload
    INFO, VALIDATION_ERROR, HARD_ERROR, SOFT_ERROR, WARNING;


}
