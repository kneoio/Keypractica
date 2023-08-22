package com.semantyca.core.service.messaging;

import com.semantyca.core.model.IDataEntity;
import com.semantyca.core.model.user.User;
import com.semantyca.core.service.messaging.email.MailAgent;
import com.semantyca.core.service.messaging.email.Memo;
import com.semantyca.core.service.messaging.exception.MsgException;

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
            MailAgent ma = new MailAgent(msgTemplate);
            Memo memo = new Memo();
            ma.sendMessage(recipients, subjectText, memo.getBody(msg));
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
