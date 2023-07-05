package com.semantyca.dto.view;


import com.semantyca.dto.cnst.SelectionMode;

import java.util.ArrayList;
import java.util.List;

public class ViewOptions {
    private List<ViewColumn> columns = new ArrayList<>();

    private SelectionMode selectionMode = SelectionMode.SINGLE;

    public void addColumn(ViewColumn column) {
        columns.add(column);
    }

    public List<ViewColumn> getColumns() {
        return columns;
    }
    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }
}
