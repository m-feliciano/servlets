package com.dev.servlet.application.transfer.records;


import com.dev.servlet.infrastructure.persistence.IPageRequest;
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

    public <TRequest> IPageRequest<TRequest> getPageRequest() {
        return (IPageRequest<TRequest>) pageRequest;
    }
}

