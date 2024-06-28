package io.kneo.core.server;

import jakarta.enterprise.context.RequestScoped;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@RequestScoped
public class OrganizationContext {
    private String organization;

}

