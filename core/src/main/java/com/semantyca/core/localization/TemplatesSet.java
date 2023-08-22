package com.semantyca.core.localization;


import com.semantyca.core.server.EnvConst;
import com.semantyca.core.server.Environment;
import com.semantyca.core.service.messaging.MessagingType;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TemplatesSet {
    private static final Logger LOGGER = Logger.getLogger(TemplatesSet.class);

    private HashMap<String, TemplateType> templs;
    private File templateDir;

    public TemplatesSet(String templatesFilePath) {
        templateDir = new File(templatesFilePath);
        templs = new HashMap<String, TemplateType>();
    }

    public static ST getRenderedTemplate(String m, Map<String, Object> variables) {
        ST rawBody = getRenderedTemplate(m);
        for (Entry<String, Object> entry : variables.entrySet()) {
            rawBody.add(entry.getKey(), entry.getValue());
        }
        return rawBody;
    }

    public static ST getRenderedTemplate(String m) {
        ST rawBody = new ST(m, '$', '$');
        rawBody.add("orgname", Environment.orgName);
        rawBody.add("appname", EnvConst.APP_ID);
        return rawBody;
    }

    public String getTemplate(MessagingType type, String templateName, LanguageCode lang) {
        TemplateType tmpl = null;
        try {
            String key = type.name() + File.separator + templateName + File.separator + lang.toString();
            tmpl = templs.get(key);
            if (tmpl == null) {
                tmpl = new TemplateType(type);
                File templateFile = new File(
                        templateDir.getAbsoluteFile() + File.separator + tmpl.getType() + File.separator + templateName
                                + File.separator + lang.toString().toLowerCase() + tmpl.getFileExt());
                if (templateFile.exists()) {
                    tmpl.content = FileUtils.readFileToString(templateFile);
                    templs.put(key, tmpl);
                } else {
                    File defaultTemplFile = new File(templateDir.getAbsoluteFile() + File.separator + tmpl.getType()
                            + File.separator + "default" + tmpl.getFileExt());
                    tmpl.content = FileUtils.readFileToString(defaultTemplFile);
                }
            }
            return tmpl.content;
        } catch (Exception e) {
            LOGGER.error(e);
            return null;
        }
    }

    public String getTemplate(MessagingType type, String templateName, LanguageCode lang,
                              HashMap<String, Object> variables) {
        return getRenderedTemplate(getTemplate(type, templateName, lang), variables).render();
    }

    public void reset() {
        templs.clear();
    }

    public StringBuffer toXML(String lang) {
        StringBuffer output = new StringBuffer(1000);

        return output;
    }

    class TemplateType {
        public String content;
        private MessagingType type;

        public TemplateType(MessagingType type2) {
            type = type2;
        }

        public String getType() {
            return type.name().toLowerCase();
        }

        public void setType(MessagingType type) {
            this.type = type;
        }

        public String getFileExt() {
            switch (type) {
                case EMAIL:
                    return ".html";
                case SITE:
                    return ".html";
                case SLACK:
                    return ".md";
                default:
                    return ".txt";
            }

        }

    }

}
