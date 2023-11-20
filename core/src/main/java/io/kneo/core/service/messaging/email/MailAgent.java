package io.kneo.core.service.messaging.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import io.kneo.core.server.Environment;
import io.kneo.core.service.messaging.MessageAgent;
import io.kneo.core.service.messaging.exception.MsgException;
import io.kneo.core.service.template.TemplateService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class MailAgent extends MessageAgent {

    @ConfigProperty(name = "mailer.sender")
    String developerEmail;
    @ConfigProperty(name = "mailer.api.key")
    String sendgridApiKey;
    private static final String MESSAGE_AGENT_NAME = "E-Mail agent";
    private static final Logger LOGGER = Logger.getLogger(MailAgent.class);

    @Inject
    TemplateService templateService;

    public MailAgent() {
        super(UUID.randomUUID().toString());
        if (Environment.mailEnable) {
            Properties props = new Properties();


        }

    }


    public CompletableFuture<Void> sendMessage(List<String> recipients, String subject, String message) throws MsgException {
        LOGGER.info("Sending message \"" + StringUtils.abbreviate(subject, 64) + "\", to " + recipients.toString());
        String htmlContent = templateService.renderEmail("Dear user");
        return CompletableFuture.runAsync(() -> {
            Email from = new Email(developerEmail);
            Email to = new Email("justaidajam@gmail.com");
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);
            SendGrid sg = new SendGrid(sendgridApiKey);
            Request request = new Request();
            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);
                LOGGER.info(response.getStatusCode());
                LOGGER.debug(response.getBody());
                LOGGER.debug(response.getHeaders());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        });
    }




}
