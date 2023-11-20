# View

## ViewPageOptions

"entity kind": [view column group]

Строка документа состоит из групп колонок.

Группы колонок состоят из колонок полей документа.



## Группы колонок (ViewColumnGroup.java)

Основное предназначение групп, это поддержка мобильной верстки.

На широких экранах разница не видна, и запись документа располагается на одной строке.

На узких экранах группы занимают всю ширину блока (100%), т.е. теперь у нас документ будет занимать строки по группам.

К примеру, 3 группы = 3 строки, 2 группы = 2 строки.

Не хотите что бы на мобильниках строка документа разбивалась на строки по группам, делайте все в одной группе.

className - можно задать стилизацию для группы (стиль на свое усмотрение).

title - текст заголовка для группы. // В разработке



## root

root опции ("root": [view column group]) применяются для заголовков колонок.

Так как данные могут быть смешанного типа, введена root опция.

root - это как основной тип документа в представлении.



## ViewColumnType.java

```
public enum ViewColumnType {
    text, date, localizedName, translate, normalizedData, attachment
}
```

- text - text )
- date - дата, в format можно указать формат вывода.
- localizedName - локализованное название (значение поля locName для языка)
- translate - вывести перевод значения
- normalizedData - если данные не обьект, а int допустим - и это на самом деле пользователь. Пользователей надо добавить в payload в виде map (id:пользователь)
- attachment - вывод иконки если есть аттач



## ViewColumn
@see ViewColumn.java



## css

css классы по сути могут быть любые.

*css file*

```
/SharedResources/nb/css/components/view-page.css
```


Для задания ширины групп колонок и колонок рекомендуется использовать классы (vw-n) из view-page.css

vw-10, vw-15, vw-20, ..., vw-90, vw-95, vw-100 = ширина в процентах (10%, 15%, ..., 100%)



## DEFAULT_ROOT - опция по умолчанию (прописана в компоненте)

Если ответ сервера не содержит опций для view, будет использована DEFAULT_ROOT

```
const DEFAULT_ROOT: IColumnOptions = {
    root: [{ columns: [{ name: 'name', value: 'name', type: 'localizedName', sort: 'both' }] }]
};
```

---


## Пример

*java*

```
ViewPageOptions result = new ViewPageOptions();

ViewColumnGroup cg = new ViewColumnGroup();
cg.add(new ViewColumn("actUser").name("act_user"));
cg.add(new ViewColumn("count"));

List<ViewColumnGroup> list = new ArrayList<>();
list.add(cg);

result.addOption("root", list);
```


*json*

```
'root': [{
    columns: [
        { name: 'act_user', value: 'actUser' },
        { name: 'count', value: 'count' }
    ]
}]
```

---

*java*

```
ViewPageOptions result = new ViewPageOptions();

ViewColumnGroup cg1 = new ViewColumnGroup();
cg1.setClassName("vw-40");
cg1.add(new ViewColumn("regNumber").name("reg_number").sortDesc().className("vw-40"));
cg1.add(new ViewColumn("title").sortBoth());
cg1.add(new ViewColumn("hasAttachment").type(ViewColumnType.attachment));

ViewColumnGroup cg2 = new ViewColumnGroup();
cg2.setClassName("vw-25");
cg2.add(new ViewColumn("demandType").name("demand_type").type(ViewColumnType.localizedName).className("vw-60"));
cg2.add(new ViewColumn("status").type(ViewColumnType.translate).className("vw-40").valueAsClass("status-"));

ViewColumnGroup cg3 = new ViewColumnGroup();
cg3.setClassName("vw-20");
cg3.add(new ViewColumn("customer").type(ViewColumnType.localizedName));

ViewColumnGroup cg4 = new ViewColumnGroup();
cg4.setClassName("vw-15");
cg4.add(new ViewColumn("tags").type(ViewColumnType.localizedName).className("vw-tags").style("return { color:it.color }"));

List<ViewColumnGroup> list = new ArrayList<>();
list.add(cg1);
list.add(cg2);
list.add(cg3);
list.add(cg4);

result.addOption("root", list);
```

*json*

```
root: [{
    className: 'vw-40',
    columns: [
        { name: 'reg_number', value: 'regNumber', type: 'text', sort: 'desc', className: 'vw-40' },
        { name: 'title', value: 'title', type: 'text', sort: 'both' },
        { value: 'hasAttachment', type: 'attachment' }
    ]
}, {
    className: 'vw-25',
    columns: [
        { name: 'demand_type', value: 'demandType', type: 'localizedName', className: 'vw-60' },
        { name: 'status', value: 'status', type: 'translate', className: 'vw-40', valueAsClass: 'status-' }
    ]
}, {
    className: 'vw-20',
    columns: [{ name: 'customer', value: 'customer', type: 'localizedName' }]
}, {
    className: 'vw-15',
    columns: [{ name: 'tags', value: 'tags', type: 'localizedName', className: 'vw-tags', style: (it: Tag) => { return { color: it.color }; } }]
}]
```
