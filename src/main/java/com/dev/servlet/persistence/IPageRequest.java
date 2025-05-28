package com.dev.servlet.persistence;

public interface IPageRequest<T> extends ISorted {
    T getFilter();

    void setFilter(T filter);

    int getInitialPage();

    int getPageSize();

    default int getFirstResult() {
        return (getInitialPage() - 1) * getPageSize();
    }
}
