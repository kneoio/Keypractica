package io.kneo.core.localization;


import io.kneo.core.localization.exception.UtilityDBException;
import io.kneo.core.localization.exception.UtilityDBExceptionType;
import io.kneo.core.repository.DataEngineConst;
import io.kneo.core.server.Environment;
import io.kneo.core.server.IUtilityDatabase;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.List;

public class Vocabulary {

    private static final Logger LOGGER = Logger.getLogger(Vocabulary.class);

    public HashMap<String, Sentence> words;

    public Vocabulary() {
        IUtilityDatabase udb = Environment.utilityDatabase;
        words = new HashMap<String, Sentence>();

        if (udb != null) {
            List<String> vp;
            try {
                vp = udb.getAllWordsGrouped();
                if (vp != null) {
                    for (String key : vp) {
                        words.put(key, udb.getWord(DataEngineConst.OVERALL_APPLICATION, key));
                    }
                } else {
                    LOGGER.fatal("There is no utility facility");

                }
            } catch (UtilityDBException e) {
                if (e.getType() == UtilityDBExceptionType.NO_TABLE) {
                    LOGGER.error(e.getAddInfo());
                }
            }
        } else {
            LOGGER.warn("Utility database has not been initialized");
        }

    }


    @Deprecated
    public Vocabulary(String appName) {
        this.words = new HashMap<String, Sentence>();
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public Vocabulary(Document doc, String appName, HashMap<String, Sentence> w) {
        this.words = (HashMap<String, Sentence>) w.clone();
        org.w3c.dom.Element root = doc.getDocumentElement();

        NodeList nodename = root.getElementsByTagName("sentence");
        for (int i = 0; i < nodename.getLength(); i++) {
            Node keyword = nodename.item(i);
            Sentence sentence = new Sentence(keyword);
            words.put(sentence.keyword, sentence);
        }
    }

    public String getWord(String keyword, LanguageCode lang) {
        try {
            Sentence sent = words.get(keyword);
            if (sent == null) {
                LOGGER.warn("Translation of word \"" + keyword + "\" to " + lang + ", has not found in vocabulary");
                return keyword;
            } else {
                return sent.words.get(lang);
            }
        } catch (Exception e) {
            return keyword;
        }
    }

}
