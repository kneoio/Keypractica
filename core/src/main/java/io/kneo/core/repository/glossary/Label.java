package io.kneo.core.repository.glossary;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.DataEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Label extends DataEntity<UUID> {
    private String name;
    private Map<LanguageCode, String> localizedNames = new HashMap<>();
    private int rank;
    private boolean isActive;
    private String category;
    private String color;

    public Label(String label) {
        super();
        name = label;
    }

    public Label() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //@Json
    public Map<LanguageCode, String> getLocalizedNames() {
        return localizedNames;
    }


    public Map<String, String> getLocNames() {
        Map<String , String> localizedNames = new HashMap<>();
        localizedNames.put("ENG","bla");
        return localizedNames;
    }

    public void setLocalizedNames(LanguageCode key, String value) {
        this.localizedNames.put(key, value);
    }

    public void setLocalizedNames(Map<LanguageCode, String> localizedNames) {
        this.localizedNames = localizedNames;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public UUID getIdentifier() {
        return null;
    }

    public static class Builder {
        private String id;
        private String name;
        private Map<LanguageCode, String> localizedNames;
        private int rank;
        private boolean isActive;
        private String category;
        private String color = String.format("#%06x", new Random().nextInt(256*256*256));

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setLocalizedNames(Map<LanguageCode, String> localizedNames) {
            this.localizedNames = localizedNames;
            return this;
        }

        public Builder setRank(int rank) {
            this.rank = rank;
            return this;
        }

        public Builder setActive(boolean active) {
            isActive = active;
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setColor(String color) {
            this.color = color;
            return this;
        }

        public Label build() {
            Label entity = new Label();
            entity.setName(name);
            entity.setRank(rank);
            entity.setActive(isActive);
            entity.setCategory(category);
            entity.setColor(color);
            entity.setLocalizedNames(localizedNames);
            return entity;
        }
    }

}
