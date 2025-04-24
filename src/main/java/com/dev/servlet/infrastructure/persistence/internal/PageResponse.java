package com.dev.servlet.infrastructure.persistence.internal;

import com.dev.servlet.domain.transfer.records.Sort;
import com.dev.servlet.infrastructure.persistence.IPageable;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PageResponse<T> implements IPageable<T> {
    private List<T> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private Sort sort;
    public int getTotalPages() {
        double totalPerPage = Math.ceil(totalElements * 1.0 / pageSize);
        return Math.max((int) totalPerPage, 1);
    }
}
