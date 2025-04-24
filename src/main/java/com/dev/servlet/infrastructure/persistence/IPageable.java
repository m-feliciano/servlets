package com.dev.servlet.infrastructure.persistence;
import java.util.List;

/**
 * Interface representing a page of data with pagination metadata.
 * Extends sorting capabilities for ordered data retrieval.
 * 
 * <p>This interface encapsulates a page of results along with pagination information
 * such as total elements count, current page number, and page size. It provides
 * a standardized way to handle paginated data across the application, enabling
 * consistent pagination patterns in all layers.</p>
 * 
 * @param <T> the type of elements contained in this page
 * @author servlets-team
 * @since 1.0
 */
public interface IPageable<T> extends ISorted {
    
    /**
     * Returns the content of this page as a list.
     *
     * @return List containing the elements of this page
     */
    List<T> getContent();
    
    /**
     * Returns the total number of elements available across all pages.
     *
     * @return the total count of elements in the entire dataset
     */
    long getTotalElements();
    
    /**
     * Returns the current page number (1-based indexing).
     *
     * @return the current page number, starting from 1
     */
    int getCurrentPage();
    
    /**
     * Returns the size of this page (number of elements per page).
     *
     * @return the maximum number of elements this page can contain
     */
    int getPageSize();
}
