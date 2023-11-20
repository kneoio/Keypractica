package io.kneo.core.service.messaging.telegram;

import io.kneo.core.service.messaging.MessageAgent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@ApplicationScoped
public class GeneralBot extends MessageAgent {

    protected String MESSAGE_AGENT_NAME = "E-Mail agent";
    private static final Logger logger = LoggerFactory.getLogger(GeneralBot.class);

    @ConfigProperty(name = "telegram.token")
    private String token;

    @ConfigProperty(name = "telegram.chatId")
    private String chatId;

    private Client client;
    private WebTarget baseTarget;

    protected GeneralBot(String id) {
        super(id);
    }

    public void sendMessage(String message) {
        Response response = baseTarget.path("sendMessage").queryParam("chat_id", chatId).queryParam("text", message)
                .request().get();
        JsonObject json = response.readEntity(JsonObject.class);
        boolean ok = json.getBoolean("ok", false);
        if (!ok) {
            logger.error("Send message failed!");
        }
    }

    @PostConstruct
    void initClient() {
        client = ClientBuilder.newClient();
        baseTarget = client.target("https://api.telegram.org/bot{token}").resolveTemplate("token", this.token);
    }

    @PreDestroy
    private void closeClient() {
        client.close();
    }

}