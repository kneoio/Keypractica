package com.semantyca.core.service;

import com.semantyca.core.dto.cnst.UserRegStatus;
import com.semantyca.core.dto.document.UserRegistrationDTO;
import com.semantyca.core.model.Module;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.model.user.User;
import com.semantyca.core.repository.UserRepository;
import com.semantyca.core.service.messaging.email.MailAgent;
import com.semantyca.core.service.template.TemplateService;
import com.semantyca.core.util.NumberUtil;
import com.semantyca.extensions.ExtensionMetaData;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger("RegistrationService");
    @Inject
    private UserRepository repository;

    @Inject
    private ModuleService moduleService;

    @Inject
    private RoleService roleService;

    @Inject
    MailAgent mailAgent;
    @Inject
    TemplateService templateService;
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public String register(UserRegistrationDTO userRegistration) {
        int confirmationCode = userRegistration.confirmationCode();
        if (confirmationCode == 0) {
            int regNumber = NumberUtil.getRandomNumber(1000,9999);
            String htmlContent = templateService.renderRegistrationEmail(regNumber);
         //   mailAgent.sendMessage(Collections.singletonList("justaidajam@gmail.com"), "registration", htmlContent).join();
            User user = new User.Builder()
                    .setEmail(userRegistration.email())
                    .setDefaultLang(userRegistration.lang().getCode())
                    .setTimeZone(userRegistration.timeZone())
                    .setConfirmationCode(regNumber)
                    .setRegStatus(UserRegStatus.WAITING_FOR_REG_CODE_CONFIRMATION)
                    .setLogin(userRegistration.login()).build();
            Uni<Long> longUni = repository.insert(user);
            longUni.subscribe().with(id -> {
                System.out.println("ok");
            }, failure -> {
                String errorMessage = failure.getMessage();
                System.out.println("Error message: " + errorMessage);
            });

            return "We have sent email, enter confirmation code ";
        } else {
            Optional<IUser> u = repository.findByLogin(userRegistration.login());
            if (u.isPresent()) {
                if (confirmationCode == u.get().getConfirmationCode()) {
                    User user = (User) u.get();
                    user.setLogin(userRegistration.login());
                    Uni<List<Module>> modulesUni = moduleService.getAll(ExtensionMetaData.defaultModules);
                    user.setModules(modulesUni.await().indefinitely());
                    user.setConfirmationCode(0);
                    user.setRegStatus(UserRegStatus.REGISTERED);
                    repository.update(user);
                    JwtClaimsBuilder issued = Jwt.issuer(issuer);
                    issued.upn(userRegistration.email());
                    issued.groups(new HashSet<>(List.of("User")));
                    return issued
                            //.expiresIn()
                            .sign();
                }
            }
        }
        return null;
    }

    public String confirmation(String confirmation) {

        return null;
    }
}
