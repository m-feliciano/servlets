package com.dev.servlet.domain.service;
import com.dev.servlet.domain.transfer.dto.DataTransferObject;
import com.dev.servlet.domain.transfer.request.Request;
import com.dev.servlet.domain.repository.ICrudRepository;

/**
 * Base service interface that provides common service operations for all entity types.
 * Extends CRUD repository functionality with additional service-layer capabilities.
 * 
 * <p>This interface serves as the foundation for all service implementations in the domain layer,
 * providing standard operations for entity manipulation, data transformation, and request handling.
 * It combines repository patterns with service-specific functionality to create a comprehensive
 * base for all business services.</p>
 * 
 * @param <T> the entity type this service manages
 * @param <ID> the type of the entity's identifier
 * @author servlets-team
 * @since 1.0
 */
public interface IBaseService<T, ID> extends ICrudRepository<T, ID> {
    
    /**
     * Extracts and converts request data into the corresponding entity object.
     * This method handles the transformation from request parameters to domain entities.
     *
     * @param request the request containing entity data
     * @return the entity object constructed from request data
     */
    T getEntity(Request request);
    
    /**
     * Converts a generic object into the specific entity type managed by this service.
     * This method provides type-safe conversion for objects that represent the same data.
     *
     * @param object the object to convert to entity type
     * @return the converted entity object
     */
    T toEntity(Object object);
    
    /**
     * Returns the class type of the Data Transfer Object (DTO) used for data mapping.
     * This method provides metadata about the DTO type associated with this service.
     *
     * @return the Class object representing the DTO type for this entity
     */
    Class<? extends DataTransferObject<ID>> getDataMapper();
}
