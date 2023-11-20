package io.kneo.core.dto.filter;

import java.util.ArrayList;
import java.util.List;


public class FilterGroup {
    public String title;
    public String className;
    private List<FilterItem> items = new ArrayList<>();

    public FilterGroup() {
    }

    public FilterGroup(String title, String className) {
        this.title = title;
        this.className = className;
    }

    public List<FilterItem> getItems() {
        return items;
    }

    public void addItem(FilterItem filterItem) {
        items.add(filterItem);
    }
}
