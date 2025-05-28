package com.dev.servlet.persistence.impl;

import com.dev.servlet.model.pojo.records.Sort;
import com.dev.servlet.persistence.IPageable;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public final class PageableImpl<T> implements IPageable<T> {
    private Iterable<T> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private Sort sort;

    public static <U, T> IPageable<U> cloneOf(IPageable<T> pageable, List<U> content) {
        return PageableImpl.<U>builder()
                .content(content)
                .totalElements(pageable.getTotalElements())
                .currentPage(pageable.getCurrentPage())
                .pageSize(pageable.getPageSize())
                .sort(pageable.getSort())
                .build();
    }

    public int getTotalPages() {
        double totalPerPage = Math.ceil(totalElements * 1.0 / pageSize);
        return Math.max((int) totalPerPage, 1);
    }
}