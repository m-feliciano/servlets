package com.dev.servlet.infrastructure.persistence;
import com.dev.servlet.domain.transfer.records.Sort;

/**
 * Interface providing sorting capabilities for data retrieval operations.
 * 
 * <p>This interface defines the contract for objects that support data sorting,
 * providing a default implementation that returns unsorted data. It serves as
 * a base interface for pagination and other data retrieval interfaces that
 * require sorting functionality.</p>
 * 
 * <p>Implementations can override the default method to provide specific
 * sorting logic based on their requirements.</p>
 * 
 * @author servlets-team
 * @since 1.0
 */
public interface ISorted {
    
    /**
     * Returns the sort criteria for data ordering.
     * The default implementation returns unsorted data.
     *
     * @return Sort object containing sorting criteria, defaults to unsorted
     */
    default Sort getSort() {
        return Sort.unsorted();
    }
}
