package com.semantyca.core.service.messaging.email;


import com.semantyca.core.localization.TemplatesSet;
import com.semantyca.core.scriptprocessor.ScriptHelper;
import com.semantyca.core.server.Environment;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import org.jboss.logging.Logger;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;


public class Memo {
    private static final Logger LOGGER = Logger.getLogger(Memo.class);

    private Multipart body = new MimeMultipart();
    private Map<String, Object> variables = new HashMap<String, Object>();

    @SuppressWarnings("unused")
    private static Iterable<Field> getFieldsUpTo(Class<?> startClass, Class<?> exclusiveParent) {

        List<Field> currentClassFields = new ArrayList<Field>(Arrays.asList(startClass.getDeclaredFields()));
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
            List<Field> parentClassFields = (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    public void addVar(String varName, Object value) {
        variables.put(varName, value);
    }

    public Multipart getBody(String message) {
        try {
            BodyPart htmlPart = new MimeBodyPart();
            if (Environment.isDevMode) {
                ScriptHelper.println(toString());
            }
            htmlPart.setContent(TemplatesSet.getRenderedTemplate(message, variables).render(),
                    "text/html; charset=utf-8");
            body.addBodyPart(htmlPart);
            return body;
        } catch (MessagingException e) {/***/
            LOGGER.error(e);
        }
        return body;
    }

    public String getPlainBody(String message) {
        if (Environment.isDevMode) {
            ScriptHelper.println(toString());
        }
        return TemplatesSet.getRenderedTemplate(message, variables).render();
    }

    public void setBody(Multipart body) {
        this.body = body;
    }

    @Override
    public String toString() {
        String result = "-----------properties of the email form-----------\n";

        for (Entry<String, Object> entry : variables.entrySet()) {
            result += entry.getKey() + " = " + entry.getValue() + "\n";
        }
        result += "-----------------------------------------------------";
        return result;

    }

}
