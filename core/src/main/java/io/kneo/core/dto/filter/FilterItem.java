package io.kneo.core.dto.filter;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterItem {

    public String name;
    public FilterType type = FilterType.text;
    public String placeholder;
    public String className;
    public String targetValue;
    public Boolean searchable;
    public Boolean multiple;
    public String idKey;
    public String textKey;
    public String listPath;
    public String groupBy;
    public Boolean groupSelectable;
    /*
     * style
     * function body. example: return { color:it.color }
     * model argument name: it
     */
    public String style;
    public String url;
    public List<Item> items;

    public FilterItem(String name) {
        this.name = name;
        this.placeholder = name;
    }

    public FilterItem(String name, String placeholder) {
        this.name = name;
        this.placeholder = placeholder;
    }

    public FilterItem name(String name) {
        this.name = name;
        return this;
    }

    public FilterItem typeSelect() {
        this.type = FilterType.select;
        return this;
    }

    public FilterItem typeCheckbox() {
        this.type = FilterType.checkbox;
        return this;
    }

    public FilterItem placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public FilterItem targetValue(String targetValue) {
        this.targetValue = targetValue;
        return this;
    }

    public FilterItem searchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public FilterItem multiple() {
        this.multiple = true;
        return this;
    }

    public FilterItem idKey(String idKey) {
        this.idKey = idKey;
        return this;
    }

    public FilterItem textKey(String textKey) {
        this.textKey = textKey;
        return this;
    }

    public FilterItem listPath(String listPath) {
        this.listPath = listPath;
        return this;
    }

    public FilterItem groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public FilterItem groupBy(String groupBy, boolean groupSelectable) {
        this.groupBy = groupBy;
        this.groupSelectable = groupSelectable;
        return this;
    }

    public FilterItem groupSelectable(boolean groupSelectable) {
        this.groupSelectable = groupSelectable;
        return this;
    }

    public FilterItem style(String style) {
        this.style = style;
        return this;
    }

    public FilterItem url(String url) {
        this.url = url;
        if (url != null && !url.isEmpty()) {
            type = FilterType.select;
            if (targetValue == null) {
                targetValue = "id";
            }
        }
        return this;
    }

    public FilterItem items(List<Item> items) {
        this.items = items;
        if (items != null && !items.isEmpty()) {
            type = FilterType.select;
        }
        return this;
    }

    private enum FilterType {text, select, checkbox}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Item {
        public String id;
        public String title;
        public String _itemClass;

        public Item(String id, String title, String _itemClass) {
            this.id = id;
            this.title = title;
            this._itemClass = _itemClass;
        }
    }
}
