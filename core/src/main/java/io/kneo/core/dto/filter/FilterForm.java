package io.kneo.core.dto.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterForm {
    private List<FilterGroup> groups = new ArrayList<>();

    public String getHash() {
        String hash = "";
        for (FilterGroup fg : this.groups) {
            for (FilterItem filterItem : fg.getItems()) {
                hash += filterItem.name;
            }
        }

        return hash;
    }

    public List<FilterGroup> getGroups() {
        return groups;
    }

    public void addGroup(FilterGroup group) {
        this.groups.add(group);
    }
}
