package com.dev.servlet.persistence;

import com.dev.servlet.model.pojo.records.Sort;

public interface ISorted {

    default Sort getSort() {
        return Sort.unsorted();
    }
}
