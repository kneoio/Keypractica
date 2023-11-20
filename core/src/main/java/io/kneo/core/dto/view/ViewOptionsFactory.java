package io.kneo.core.dto.view;


import java.util.ArrayList;
import java.util.List;


public class ViewOptionsFactory {

    public static ViewOptions getDefaultOptions() {
        ViewOptions viewOptions = new ViewOptions();
        viewOptions.addColumn(new ViewColumn.Builder().setName ("close").build());
        return viewOptions;
    }

    public static ViewOptions getProjectOptions() {
        ViewOptions viewOptions = new ViewOptions();
        List<ViewColumnGroup> project = new ArrayList<>();
        viewOptions.addColumn(new ViewColumn.Builder().setName ("name").build());
        viewOptions.addColumn(new ViewColumn.Builder().setName ("status").build());
        viewOptions.addColumn(new ViewColumn.Builder().setName ("customer").build());
        viewOptions.addColumn(new ViewColumn.Builder().setName ("finish_date").setType(ViewColumnType.DATE).build());
        return viewOptions;
    }

}
