package io.kneo.projects.dto.ai;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PromptDTO {

    @NotBlank(message = "Prompt text cannot be blank")
    @Size(max = 1000, message = "Prompt text cannot exceed 1000 characters")
    private String promptText;

    private String context;

    private Integer maxTokens;

    private Double temperature;

    // Getters and setters

    public String getPromptText() {
        return promptText;
    }

    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
}