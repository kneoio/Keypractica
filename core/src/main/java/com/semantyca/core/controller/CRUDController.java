
package com.semantyca.core.controller;

import com.semantyca.core.dto.IDTO;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CRUDController<T extends IDTO> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    JsonWebToken jwt;





}
