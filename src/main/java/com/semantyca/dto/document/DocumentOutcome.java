package com.semantyca.dto.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.dto.AbstractOutcome;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentOutcome extends AbstractOutcome<DocumentOutcome> {

}
