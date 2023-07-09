package com.semantyca.core.dto.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.semantyca.core.server.EnvConst;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"count", "pageNum", "maxPage", "pageSize", "keyword", "entries"})
public class View<T> {
    private List<T> entries;
    private long count;
    private int maxPage;
    private int pageNum;
    private final int pageSize;
    private String keyword;


    public View(List<T> entries, long count, int maxPage, int pageNum, int pageSize) {
        this.entries = entries;
        this.count = count;
        this.maxPage = maxPage;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public View(List<T> entries, long count, int maxPage, int pageNum, int pageSize, String keyword) {
        this.entries = entries;
        this.count = count;
        this.maxPage = maxPage;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.keyword = keyword;
    }

    public View(List<T> entries) {
         this.entries = entries;
        this.count = entries.size();
        this.maxPage = 1;
        this.pageNum = 1;
        this.pageSize = (int) count;
    }

    public View() {
        this(new ArrayList<>(), 0, 1, 1, EnvConst.DEFAULT_PAGE_SIZE);
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<T> getEntries() {
        return entries;
    }

    public void setEntries(List<T> entries) {
        this.entries = entries;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getKeyword() {
        return keyword;
    }

}
