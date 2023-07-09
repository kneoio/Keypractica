
package com.semantyca.core.controller;

import com.semantyca.core.dto.IDTO;
import com.semantyca.core.dto.cnst.PayloadType;
import com.semantyca.core.dto.view.View;
import com.semantyca.core.dto.view.ViewOptionsFactory;
import com.semantyca.core.dto.view.ViewPage;
import com.semantyca.core.service.IBasicService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CRUDController<T extends IDTO> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    JsonWebToken jwt;


    public Response get(IBasicService service)  {
        ViewPage viewPage = new ViewPage();
        viewPage.addPayload(PayloadType.VIEW_OPTIONS, ViewOptionsFactory.getProjectOptions());
        View<T> view = new View<>();
        viewPage.addPayload(PayloadType.VIEW_DATA, view);
        return Response.ok(viewPage).build();
    }


}
