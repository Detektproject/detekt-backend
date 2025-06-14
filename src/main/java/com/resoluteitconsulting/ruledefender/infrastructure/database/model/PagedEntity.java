package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import lombok.Getter;

import java.util.List;

@Getter
public class PagedEntity<T> {

    private List<T> items;
    private int pageCount;
    private int pageIndex;

    public PagedEntity<T> items(List<T> items) {
        this.items = items;
        return this;
    }

    public PagedEntity<T> pageCount(int pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    public PagedEntity<T> pageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
        return this;
    }
}
