package io.kneo.core.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.kneo.core.dto.cnst.MessageLevel;
import io.kneo.core.dto.cnst.OutcomeType;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"identifier", "type", "title", "pageName", "payloads"})
public class ProcessFeedback extends AbstractPage {
    List<FeedbackEntry> entries = new ArrayList<>();

    public void addEntry(FeedbackEntry entry){
        entries.add(entry);
    }

    public void addEntry(String id, MessageLevel level, String descr){
        FeedbackEntry entry = new FeedbackEntry();
        entry.setId(id);
        entry.setLevel(level);
        entry.setDescription(descr);
        entries.add(entry);
    }

    @JsonGetter("title")
    public String getTitle() {
        return "server processing feedback";
    }

    @JsonGetter("type")
    public OutcomeType getType() {
        return OutcomeType.INFO;
    }


}
