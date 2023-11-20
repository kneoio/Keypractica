package io.kneo.projects;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@OpenAPIDefinition(
        tags = {
                @Tag(name="core", description="Core operations"),
                @Tag(name="officeframe", description="Officeframe operations")
        },
        info = @Info(
                title="Keypractica",
                version = "1.0.1",
                contact = @Contact(
                        name = "syspo",
                        url = "http://keypractica.com",
                        email = "biosubj@gmail.com"),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"))
)
public class ApiApplication extends Application {
}
