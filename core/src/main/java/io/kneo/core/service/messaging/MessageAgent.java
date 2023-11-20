package io.kneo.core.service.messaging;

import org.jboss.logging.Logger;

public abstract class MessageAgent {
    private static final Logger LOGGER = Logger.getLogger(MessageAgent.class);

    protected String MESSAGE_AGENT_NAME = "Undefined";

    protected String messageId;

    protected MessageAgent(String id) {
       /* uDao = new UserDAO(new _Session(new SuperUser()));
        this.messageId = id;
        SubscriptionDAO sDao = new SubscriptionDAO();
        subscription = sDao.findByName(id);
        if (subscription == null) {
            subscription = new Subscription();
            subscription.setName(messageId);
            subscription.setOn(true);
            try {
                sDao.add(subscription);
            } catch (SecureException | DAOException e) {
                logger.exception(MESSAGE_AGENT_NAME, e);
            }
        }*/
    }
}
