package com.dev.servlet.model;

/**
 * This class is a generic identifier for the entities.
 *
 * @param <U> Identifier type
 */
public interface Identifier<U> {
    /**
     * Get the identifier
     *
     * @return identifier {@linkplain U}
     */
    U getId();

    /**
     * Set the identifier
     *
     * @param id identifier {@linkplain U}
     */
    void setId(U id);
}
