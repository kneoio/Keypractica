package io.kneo.api;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;


public class CustomOASFilter implements OASFilter {

    @Override
    public PathItem filterPathItem(PathItem pathItem) {
        pathItem.setPUT(null);
        pathItem.setDELETE(null);
        pathItem.setPATCH(null);
        return pathItem;
    }


    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        Paths paths = openAPI.getPaths();
        paths.removePathItem("/");
        paths.removePathItem("/users");
        paths.removePathItem("/departments");
        paths.removePathItem("/languages");
        paths.removePathItem("/modules");
        paths.removePathItem("/logout");
        paths.removePathItem("/translations");
        paths.removePathItem("/orgcategories");
        paths.removePathItem("/positions");
        paths.removePathItem("/roles");
        paths.removePathItem("/workspace");

        Components components = openAPI.getComponents();
        components.removeSchema("RLS");
        components.removeSchema("RLSDTO");
        components.removeSchema("ModuleDTO");
        components.removeSchema("AccessLevel");
        components.removeSchema("IRole");


    }
}


