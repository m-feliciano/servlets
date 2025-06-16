package com.dev.servlet.infrastructure.persistence;

import com.dev.servlet.application.transfer.records.Sort;

public interface ISorted {

    default Sort getSort() {
        return Sort.unsorted();
    }
}