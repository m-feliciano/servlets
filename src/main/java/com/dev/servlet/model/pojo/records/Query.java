package com.dev.servlet.model.pojo.records;

import com.dev.servlet.persistence.IPageRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * @since 1.4
 */
@Getter
@Builder
@AllArgsConstructor
@SuppressWarnings("unchecked")
public final class Query {
    private final IPageRequest<?> pageRequest;
    private final String search;
    private final String type;

    public <T> IPageRequest<T> getPageRequest() {
        return (IPageRequest<T>) pageRequest;
    }
}