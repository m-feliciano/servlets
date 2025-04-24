package com.dev.servlet.domain.repository;
import com.dev.servlet.core.mapper.Mapper;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import com.dev.servlet.infrastructure.persistence.IPageable;

/**
 * Interface for providing pagination capabilities to repositories.
 * 
 * <p>This interface defines methods for retrieving paginated data from the data store,
 * supporting both direct entity pagination and transformed data pagination using mappers.
 * It enables efficient handling of large datasets by allowing retrieval of data in
 * smaller, manageable chunks.</p>
 * 
 * @param <TData> the type of data this pagination interface handles
 * @author servlets-team
 * @since 1.0
 */
public interface IPagination<TData> {
    
    /**
     * Retrieves a page of entities based on the provided page request parameters.
     *
     * @param pageRequest the pagination parameters including page number, size, and sorting
     * @return IPageable containing the requested page of data with metadata
     */
    IPageable<TData> getAllPageable(IPageRequest<TData> pageRequest);
    
    /**
     * Retrieves a page of entities and transforms them using the provided mapper.
     * This method allows conversion of entities to DTOs or other representations
     * during the pagination process for improved performance.
     *
     * @param <TMapper> the target type after mapping transformation
     * @param pageRequest the pagination parameters including page number, size, and sorting
     * @param mapper the mapper function to transform entities to the target type
     * @return IPageable containing the requested page of mapped data with metadata
     */
    <TMapper> IPageable<TMapper> getAllPageable(IPageRequest<TData> pageRequest, Mapper<TData, TMapper> mapper);
}
