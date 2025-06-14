package com.dev.servlet.infrastructure.persistence;

import com.dev.servlet.application.dto.records.Sort;

public interface ISorted {

    default Sort getSort() {
        return Sort.unsorted();
    }
}