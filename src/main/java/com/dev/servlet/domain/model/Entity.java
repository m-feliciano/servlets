package com.dev.servlet.domain.model;

/**
 * Base entity interface providing common identifier functionality for all domain entities.
 * 
 * <p>This interface defines the contract that all domain entities must implement,
 * providing a standardized way to handle entity identifiers across the application.
 * It enables generic operations on entities regardless of their specific type,
 * supporting consistent entity management patterns throughout the domain layer.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * public class User implements Entity<Long> {
 *     private Long id;
 *     
 *     @Override
 *     public Long getId() { return id; }
 *     
 *     @Override
 *     public void setId(Long id) { this.id = id; }
 * }
 * }</pre>
 * 
 * @param <U> the type of the entity's unique identifier
 * @author servlets-team
 * @since 1.0
 */
public interface Entity<U> {
    
    /**
     * Returns the unique identifier of this entity.
     *
     * @return the entity's unique identifier
     */
    U getId();
    
    /**
     * Sets the unique identifier for this entity.
     *
     * @param id the unique identifier to set
     */
    void setId(U id);
}
