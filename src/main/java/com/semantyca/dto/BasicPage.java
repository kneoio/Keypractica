package com.semantyca.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.semantyca.model.phrase.Phrase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"title", "content", "links"})
public class BasicPage implements IPage, IDTO {
    private String title;
    private List<Phrase> content = new ArrayList<>();

    private Map<String, String> links = new HashMap();

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void addPhrase(Phrase phrase) {
        content.add(phrase);
    }

    @Override
    public List<Phrase> getContent() {
        return content;
    }
    @Override
    public String getType() {
        return null;
    }

    @Override
    public Map getLinks() {
       links.put("start", "v1/start");
       links.put("next", "v1/next");
       links.put("previous", "v1/previous");
       links.put("end", "v1/end");
       return links;
    }

}
