
package com.semantyca.core.controller;

import com.semantyca.core.dto.IDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSecuredController<T extends IDTO> {
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());


}
