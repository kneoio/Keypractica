package com.semantyca.core.dto.view;


import java.util.ArrayList;
import java.util.List;


public class ViewOptionsFactory {

    public static ViewOptions getProjectOptions() {
        ViewOptions viewOptions = new ViewOptions();
        List<ViewColumnGroup> project = new ArrayList<>();
        viewOptions.addColumn(new ViewColumn.Builder().setName ("name").build());
        viewOptions.addColumn(new ViewColumn.Builder().setName ("status").build());
        viewOptions.addColumn(new ViewColumn.Builder().setName ("customer").build());
        viewOptions.addColumn(new ViewColumn.Builder().setName ("finish_date").setType(ViewColumnType.DATE).build());
        return viewOptions;
    }

   /* public ViewOptions getTaskViewOptions() {
        ViewOptions result = new ViewOptions();

        ViewColumnGroup tcg1 = new ViewColumnGroup();
        tcg1.setClassName("vw-35 vw-md-70");
        tcg1.add(new ViewColumn("regNumber").name("reg_number").className("vw-45"));
        tcg1.add(new ViewColumn("status").type(ViewColumnType.translate).sortBoth().className("vp__list_it_col-if-tree-view status-col-mobile").valueAsClass("status-bg-"));
        tcg1.add(new ViewColumn("title").name("task_title").sortBoth().style(" if(it.approvalStatus === 'PENDING') { return { color:'#9C27B0', 'font-weight':'bold' } }"));
        tcg1.add(new ViewColumn().type(ViewColumnType.attachment));

        ViewColumnGroup tcg2 = new ViewColumnGroup();
        tcg2.setClassName("vw-15 vw-md-30");
        tcg2.add(new ViewColumn("status").type(ViewColumnType.translate).sortBoth().className("vw-50").valueAsClass("status-"));
        tcg2.add(new ViewColumn("priority").type(ViewColumnType.translate).sortBoth().className("vw-50").valueAsClass("priority-"));

        ViewColumnGroup tcg3 = new ViewColumnGroup();
        tcg3.setClassName("vw-10");
        tcg3.add(new ViewColumn("employees.assigneeUserId.name").name("assignee_user").type(ViewColumnType.normalizedData));

        ViewColumnGroup tcg4 = new ViewColumnGroup();
        tcg4.setClassName("vw-15");
        tcg4.add(new ViewColumn("project.name").name("project"));

        ViewColumnGroup tcg5 = new ViewColumnGroup();
        tcg5.setClassName("vw-15");
        tcg5.add(new ViewColumn("dueDate").name("due_date").type(ViewColumnType.date).format("DD.MM.YYYY").sortBoth().className("vw-50"));

        ViewColumnGroup tcg6 = new ViewColumnGroup();
        tcg6.setClassName("vw-10");
        tcg6.add(new ViewColumn("tags").type(ViewColumnType.localizedName).style("return { color: it.color }"));

        List<ViewColumnGroup> taskCols = new ArrayList<>();
        taskCols.add(tcg1);
        taskCols.add(tcg2);
        taskCols.add(tcg3);
        taskCols.add(tcg4);
        taskCols.add(tcg5);
        taskCols.add(tcg6);

        // Request
        ViewColumnGroup rcg1 = new ViewColumnGroup();
        rcg1.setClassName("vp__list_it-inline");
        rcg1.add(new ViewColumn("requestType").type(ViewColumnType.localizedName).className("request__type"));
        rcg1.add(new ViewColumn("regDate").type(ViewColumnType.date).className("request__time hidden-md"));
        rcg1.add(new ViewColumn("comment").className("request__comment hidden-md"));
        rcg1.add(new ViewColumn().type(ViewColumnType.attachment));
        rcg1.add(new ViewColumn("resolution").type(ViewColumnType.translate).className("request-list__item").valueAsClass("request-status-"));
        rcg1.add(new ViewColumn("resolutionTime").type(ViewColumnType.date).className("request__resolution_time hidden-md"));

        List<ViewColumnGroup> request = new ArrayList<>();
        request.add(rcg1);

        result.setRoot(taskCols);
        result.add("Request", request);
        return result;
    }*/

  /*  public FilterForm getProjectFilter(_Session session) {
        List<FilterItem.Item> items = new ArrayList<>();
        for (ProjectStatusType type : ProjectStatusType.getActualValues()) {
            String name = Environment.vocabulary.getWord(type.name().toLowerCase(), session.getLang());
            items.add(new FilterItem.Item(type.name(), name, "status-" + type.name().toLowerCase()));
        }

        FilterForm filterForm = new FilterForm();
        FilterGroup filterGroup = new FilterGroup();
        filterGroup.addItem(new FilterItem("status").items(items));

        filterForm.addGroup(filterGroup);

        return filterForm;
    }

    public FilterForm getTaskFilter(_Session session, String slug) {
        StatusType[] statusTypes = {
                StatusType.OPEN,
                StatusType.PROCESSING,
                StatusType.PENDING,
                StatusType.WAITING,
                StatusType.COMPLETED,
                StatusType.CANCELLED,
                StatusType.DRAFT
        };
        List<FilterItem.Item> statusTypeItems = new ArrayList<>();
        for (StatusType type : statusTypes) {
            String name = Environment.vocabulary.getWord(type.name().toLowerCase(), session.getLang());
            statusTypeItems.add(new FilterItem.Item(type.name(), name, "status-" + type.name().toLowerCase()));
        }

        List<FilterItem.Item> priorityTypeItems = new ArrayList<>();
        for (PriorityType priorityType : PriorityType.values()) {
            if (priorityType == PriorityType.UNKNOWN) {
                continue;
            }
            String name = Environment.vocabulary.getWord(priorityType.name().toLowerCase(), session.getLang());
            priorityTypeItems.add(new FilterItem.Item(priorityType.name(), name, "priority-" + priorityType.name().toLowerCase()));
        }

        FilterForm filterForm = new FilterForm();
        FilterGroup filterGroup = new FilterGroup();
        filterGroup.addItem(new FilterItem("status").items(statusTypeItems));
        filterGroup.addItem(new FilterItem("priority").items(priorityTypeItems));
        filterGroup.addItem(new FilterItem("taskType", "task_type").url("/Reference/api/task-types").searchable(false));
        if (!"my".equals(slug)) {
            filterGroup.addItem(new FilterItem("author", "author").targetValue("userID").url("/Staff/api/employees"));
        }
        if (!"inbox".equals(slug)) {
            if (session.getUser().getRoles().contains(ROLE_PRJ_TASK_MODERATOR)) {
                filterGroup.addItem(new FilterItem("assigneeUser", "assignee_user").targetValue("userID").url("/Staff/api/employees"));
            } else {
                filterGroup.addItem(new FilterItem("assigneeUser", "assignee_user").targetValue("userID").url("/Projects/api/tasks/preferredAssignees"));
            }
        }
        filterGroup.addItem(new FilterItem("project").url("/Projects/api/projects"));
        filterGroup.addItem(new FilterItem("tags").multiple().url("/Reference/api/tags?hidden=true&category=software_developing_task").style("return {color:it.color}"));

        filterForm.addGroup(filterGroup);

        return filterForm;
    }*/
}
