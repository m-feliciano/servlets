package com.dev.servlet.persistence.impl;

import com.dev.servlet.model.pojo.records.Sort;
import com.dev.servlet.persistence.IPageRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public final class PageRequestImpl<T> implements IPageRequest<T> {
    private Sort sort;
    private int initialPage;
    private int pageSize;

    @Setter
    private T filter;
}