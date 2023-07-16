package com.semantyca.core.dto.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.semantyca.core.server.EnvConst;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"count", "pageNum", "maxPage", "pageSize", "keyword", "entries"})
@Setter
@Getter
public class View<T> {
    private List<T> entries;
    private long count;
    private int maxPage;
    private int pageNum;
    private int pageSize;
    private String keyword;

    public View(List<T> entries, Integer count, Integer pageNum, Integer maxPage, int pageSize) {
        this.entries = entries;
        this.count = count;
        this.maxPage = maxPage;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public View(List<T> entries) {
        this.entries = entries;
        this.count = entries.size();
        this.maxPage = 999;
        this.pageNum = 1;
        this.pageSize = EnvConst.DEFAULT_PAGE_SIZE;

    }
}
