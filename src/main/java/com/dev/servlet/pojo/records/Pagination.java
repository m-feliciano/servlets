package com.dev.servlet.pojo.records;

import com.dev.servlet.pojo.enums.Order;
import com.dev.servlet.pojo.enums.Sort;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.NONE)
@Builder
@AllArgsConstructor
public class Pagination implements java.io.Serializable {
    @Setter(AccessLevel.PUBLIC)
    private Integer totalRecords;
    private final Integer currentPage;
    private final Integer pageSize;
    private final Sort sort;
    private final Order order;

    public int getTotalPages() {
        double totalPerPage = Math.ceil(totalRecords * 1.0 / pageSize);
        return Math.max((int) totalPerPage, 1);
    }

    public int getFirstResult() {
        return (currentPage - 1) * pageSize;
    }

}