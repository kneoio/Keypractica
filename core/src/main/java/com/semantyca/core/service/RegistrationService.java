package com.semantyca.core.service;

import com.semantyca.core.dto.document.UserRegistrationDTO;
import com.semantyca.core.model.Module;
import com.semantyca.core.model.user.User;
import com.semantyca.core.repository.UserRepository;
import com.semantyca.extensions.ExtensionMetaData;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class RegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger("RegistrationService");
    @Inject
    private UserRepository repository;

    @Inject
    private ModuleService moduleService;

    @Inject
    private RoleService roleService;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public String register(UserRegistrationDTO userRegistration) {
        if (userRegistration.confirmationCode() != 0) {
            Uni<List<Module>> modules = moduleService.getAll(ExtensionMetaData.defaultModules);
            User user = new User.Builder()
                    .setEmail(userRegistration.email())
                    .setDefaultLang(userRegistration.lang().getCode())
                    .setTimeZone(userRegistration.timeZone())
                    //.setModules(modules.onItem().transform(v -> v))
                    .setLogin(userRegistration.userName()).build();

            Uni<Long> longUni = repository.insert(user);

            String token = Jwt.issuer(issuer)
                    .upn(userRegistration.email())
                    .groups(new HashSet<>(Arrays.asList("User")))
                    //.expiresIn()
                    .sign();
            return "uyiyiu";
        } else {
            return "we have sent email";
        }

    }

}
