package com.semantyca.core.service.messaging.email;

import com.semantyca.core.server.EnvConst;
import com.semantyca.core.server.Environment;
import com.semantyca.core.service.messaging.MessageAgent;
import com.semantyca.core.service.messaging.exception.MsgException;
import com.sun.mail.util.MailConnectException;
import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class MailAgent extends MessageAgent {

    private static final Logger LOGGER = Logger.getLogger(MailAgent.class);

    protected String MESSAGE_AGENT_NAME = "E-Mail agent";
    private Session mailerSes;

    public MailAgent(String id) {
        super(id);
        if (Environment.mailEnable) {
            Properties props = new Properties();
            props.put("mail.smtp.host", Environment.smtpHost);
            props.put("mail.smtp.port", Environment.smtpPort);
            if (Environment.smtpAuth) {
                props.put("mail.smtp.auth", Environment.smtpAuth);
                if ("465".equals(Environment.smtpPort)) {
                    props.put("mail.smtp.ssl.enable", "true");
                }
                Authenticator auth = new SMTPAuthenticator();
                mailerSes = Session.getInstance(props, auth);
            } else {
                mailerSes = Session.getInstance(props, null);
            }

        }

    }

    public boolean sendMessage(List<String> recipients, String subject, Multipart mailMessage) throws MsgException {
        LOGGER.info("Sending message \"" + StringUtils.abbreviate(subject, 64) + "\", to " + recipients.toString());
        return sendMessage(recipients, subject, mailMessage, true);
    }


    public boolean sendMessage(List<String> recipients, String subject, Multipart mailMessage,
                               boolean checkUserSubscription) throws MsgException {
        MimeMessage msg = new MimeMessage(mailerSes);

        try {
            msg.setContent(mailMessage);
            msg.setSubject(subject, "utf-8");
        } catch (MessagingException e) {
            String error = e.toString();
            LOGGER.error(e);
            throw new MsgException(error);
        }

        RunnableFuture<Boolean> f = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return send(msg, recipients);
            }
        });
        new Thread(f).start();
        // return f.connect();
        return true;
    }

    public boolean sendMessageSync(List<String> recipients, String subject, Multipart mailMessage,
                                   boolean checkUserSubscription) throws MsgException {
        MimeMessage msg = new MimeMessage(mailerSes);

        try {
            msg.setContent(mailMessage);
            msg.setSubject(subject, "utf-8");
        } catch (MessagingException e) {
            String error = e.toString();
            LOGGER.error(MESSAGE_AGENT_NAME, e);
            throw new MsgException(error);
        }

        return send(msg, recipients);

    }

    private boolean send(MimeMessage msg, List<String> recipients) throws MsgException {
        try {
            msg.setFrom(new InternetAddress(Environment.smtpUser, Environment.smtpUserName));
            for (String recipient : recipients) {
                try {
                    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                } catch (AddressException ae) {
                    LOGGER.warn("Incorrect e-mail \"" + recipient + "\"");
                    continue;
                }
            }
        } catch (MessagingException e) {
            LOGGER.error(MESSAGE_AGENT_NAME, e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(MESSAGE_AGENT_NAME, e);
        }

        try {
            if (Environment.mailEnable) {
                boolean isAllowToSend = false;
                if (!Environment.isDevMode) {
                    Transport.send(msg);
                } else {
                    StringJoiner r = new StringJoiner(":");
                    for (Address recipient : msg.getRecipients(Message.RecipientType.TO)) {
                        r.add(recipient.toString());
                        if (recipient.toString().equals(EnvConst.DEVELOPER_EMAIL)) {
                            isAllowToSend = true;
                        }
                    }
                    if (isAllowToSend) {
                        Transport.send(msg);
                    } else {
                        LOGGER.debug("Imitate message sent:" + r + " " + msg.getSubject());
                    }
                }

                LOGGER.info("Message has been sent to " + recipients);
                return true;
            } else {
                LOGGER.warn("Mail agent disabled");
            }
        } catch (MailConnectException e) {
            LOGGER.error(MESSAGE_AGENT_NAME, e);
        } catch (SendFailedException se) {
            if (se.getMessage().contains("relay rejected for policy reasons")) {
                String error = "relay rejected for policy reasons by SMTP server. Message has not sent";
                LOGGER.warn(error);
                throw new MsgException(error);
            } else if (se.getMessage().contains("No recipient addresses")) {
//				Lg.exception(se);
                String error = "No recipient addresses. Message has not sent. Recipients=" + recipients;
                LOGGER.warn(error);
                throw new MsgException(error);
            } else {
                String error = "unable to send a message, probably SMTP host did not set";
                LOGGER.error(error);
                LOGGER.error(MESSAGE_AGENT_NAME, se);
                throw new MsgException(error);
            }
        } catch (AuthenticationFailedException e) {
            String error = "SMTP authentication exception, smtp user=" + Environment.smtpUser;
            LOGGER.error(MESSAGE_AGENT_NAME, e);
            throw new MsgException(error);
        } catch (MessagingException e) {
            String error = e.toString();
            LOGGER.error(MESSAGE_AGENT_NAME, e);
            throw new MsgException(error);
        }
        return false;

    }

    class SMTPAuthenticator extends Authenticator {
        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(Environment.smtpUser, Environment.smtpPassword);
        }
    }

}
