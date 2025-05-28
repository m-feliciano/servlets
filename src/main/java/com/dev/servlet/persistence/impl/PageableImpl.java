package com.dev.servlet.persistence.impl;

import com.dev.servlet.model.pojo.records.Sort;
import com.dev.servlet.persistence.IPageable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class PageableImpl<T> implements IPageable<T> {
    private Iterable<T> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private Sort sort;

    public int getTotalPages() {
        double totalPerPage = Math.ceil(totalElements * 1.0 / pageSize);
        return Math.max((int) totalPerPage, 1);
    }
}