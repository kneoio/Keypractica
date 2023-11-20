package io.kneo.core.dto.view;

import java.util.ArrayList;
import java.util.List;

public class ViewColumnGroup {

    private String title;
    private List<ViewColumn> columns = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ViewColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<ViewColumn> columns) {
        this.columns = columns;
    }

    public void add(ViewColumn column) {
        this.columns.add(column);
    }
}
