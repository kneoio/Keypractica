package io.kneo.core.service.messaging;

import io.kneo.core.model.IDataEntity;
import io.kneo.core.model.user.User;
import io.kneo.core.service.messaging.email.MailAgent;
import io.kneo.core.service.messaging.exception.MsgException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kaira on 12/7/17.
 */
public class MessagingHelper {

    public static void sendInAnyWay(User recipient, String msg, IDataEntity entityForLink, String subjectText) throws MsgException {
        String msgTemplate = "generic_message";

        List<String> recipients = new ArrayList<>();
        String email = recipient.getEmail();
        if (email != null) {
            recipients.add(email);
            MailAgent ma = new MailAgent();

            ma.sendMessage(recipients, subjectText,msg);
        }
    }

   /* public static void sendInAnyWay(AppEnv appEnv, User user, Memo memo, String msgTemplate, IDataEntity entityForLink, String subjectText) throws MsgException {
        if (user != null) {
            LanguageCode lang = LanguageCode.getType(user.getDefaultLang());

            memo.addVar("url", Environment.getFullHostName() + "/" + appEnv.appName + "/#" + entityForLink.getURL() + "?lang=" + lang);


            List<String> recipients = new ArrayList<>();
            String email = user.getEmail();
            if (email != null) {
                recipients.add(email);
                MailAgent ma = new MailAgent(msgTemplate);
                ma.sendMessage(recipients, Environment.vocabulary.getWord(subjectText, lang),
                        memo.getBody(appEnv.templates.getTemplate(MessagingType.EMAIL, msgTemplate, lang)));
                Environment.getActivityRecorder().postEmailSending(entityForLink, recipients, subjectText);
            }
        }
    }*/

}
