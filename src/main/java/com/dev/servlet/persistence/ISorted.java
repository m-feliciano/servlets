package com.dev.servlet.persistence;

import com.dev.servlet.model.pojo.records.Sort;

import java.io.Serializable;

public interface ISorted extends Serializable {

    default Sort getSort() {
        return Sort.unsorted();
    }
}
