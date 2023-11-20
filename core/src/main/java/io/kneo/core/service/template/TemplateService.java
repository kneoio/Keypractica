package io.kneo.core.service.template;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TemplateService {
    @Inject
    Engine engine;

    public String renderEmail(String name) {
        Template template = engine.getTemplate("basic_email");
        return template.data("name", name).render();
    }

    public String renderRegistrationEmail(int confirmationCode) {
        Template template = engine.getTemplate("registration_email");
        return template.data("confirmation_code", confirmationCode).render();
    }
}
