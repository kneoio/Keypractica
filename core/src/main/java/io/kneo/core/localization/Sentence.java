package io.kneo.core.localization;


import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Sentence  {
    public String app;
    public HashMap<LanguageCode, String> words = new HashMap<LanguageCode, String>();

    public String kind = "sentence";
    public String id;
    public String keyword;
    public LanguageCode language;
    public String word;

    public List<String> apps = new ArrayList<>();

    public Sentence() {
    }

    public Sentence(String app, String keyword, HashMap<LanguageCode, String> words) {
        this.app = app;
        this.keyword = keyword;
        this.words = words;
    }

    public Sentence(String id, String keyword, LanguageCode language, String word, String app) {
        this.id = id;
        this.keyword = keyword;
        this.language = language;
        this.word = word;
        // this.app = app;
        this.apps.add(app);
    }

    public Sentence(Node keyword) {
    }


    @Override
    public String toString() {
        return "keyword=" + keyword + ", words=" + words;
    }
}
