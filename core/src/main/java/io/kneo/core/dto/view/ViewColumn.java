package io.kneo.core.dto.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class ViewColumn {
    private String name;
    private ViewColumnType type;
    private String hint;
    private Sort sort;

    public static class Builder {
        private String name;
        private ViewColumnType type = ViewColumnType.TEXT;
        private String hint;
        private Sort sort;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(ViewColumnType type) {
            this.type = type;
            return this;
        }

        public Builder setHint(String hint) {
            this.hint = hint;
            return this;
        }

        public Builder setSort(Sort sort) {
            this.sort = sort;
            return this;
        }

        public ViewColumn build() {
            ViewColumn column = new ViewColumn();
            column.setName(name);
            column.setHint(hint);
            column.setType(type);
            column.setSort(sort);
            return column;
        }
    }
    private enum Sort {asc, desc, both}
}
