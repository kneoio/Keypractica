package io.kneo.core.localization;


import io.kneo.core.service.messaging.MessagingType;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;

import java.io.File;
import java.util.HashMap;

public class TemplatesSet {
    private static final Logger LOGGER = Logger.getLogger(TemplatesSet.class);

    private HashMap<String, TemplateType> templs;
    private File templateDir;

    public TemplatesSet(String templatesFilePath) {
        templateDir = new File(templatesFilePath);
        templs = new HashMap<String, TemplateType>();
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
