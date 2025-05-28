package com.dev.servlet.persistence;

public interface IPageable<T> extends ISorted {

    Iterable<T> getContent();

    long getTotalElements();

    int getCurrentPage();

    int getPageSize();
}
