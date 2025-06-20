package com.dev.servlet.infrastructure.persistence.internal;

import com.dev.servlet.application.transfer.records.Sort;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Builder
public final class PageRequestImpl<T> implements IPageRequest<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Sort sort;
    private int initialPage;
    private int pageSize;

    @Setter
    private T filter;
}