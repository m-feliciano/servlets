package com.dev.servlet.infrastructure.persistence.internal;
import com.dev.servlet.domain.transfer.records.Sort;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import lombok.Builder;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public final class PageRequest<T> implements IPageRequest<T>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Sort sort;
    private int initialPage;
    private int pageSize;
    private T filter;
}
