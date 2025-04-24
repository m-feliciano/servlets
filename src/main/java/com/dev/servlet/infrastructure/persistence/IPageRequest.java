package com.dev.servlet.infrastructure.persistence;

/**
 * Interface representing a request for paginated data with filtering and sorting capabilities.
 * Extends sorting functionality to support ordered data retrieval.
 * 
 * <p>This interface defines the contract for pagination requests, encapsulating
 * page parameters, filtering criteria, and sorting options. It provides a standardized
 * way to request paginated data from repositories and services, enabling consistent
 * pagination behavior across the application.</p>
 * 
 * @param <T> the type of filter object used for data filtering
 * @author servlets-team
 * @since 1.0
 */
public interface IPageRequest<T> extends ISorted {
    
    /**
     * Returns the filter object containing criteria for data filtering.
     *
     * @return the filter object of type T, or null if no filtering is applied
     */
    T getFilter();
    
    /**
     * Sets the filter object for data filtering.
     *
     * @param filter the filter object containing filtering criteria
     */
    void setFilter(T filter);
    
    /**
     * Returns the initial page number for pagination (1-based indexing).
     *
     * @return the page number to retrieve, starting from 1
     */
    int getInitialPage();
    
    /**
     * Returns the number of elements per page.
     *
     * @return the page size (number of elements to retrieve per page)
     */
    int getPageSize();
    
    /**
     * Calculates the first result index for database queries (0-based indexing).
     * This is typically used for OFFSET in SQL queries or similar operations.
     *
     * @return the zero-based index of the first result to retrieve
     */
    default int getFirstResult() {
        return (getInitialPage() - 1) * getPageSize();
    }
}
