package com.dev.servlet.pojo;

/**
 * This class is a generic identifier for the entities.
 *
 * @param <U> Identifier type
 */
public interface Identifier<U> {
    /**
     * Get the identifier
     *
     * @return identifier {@link U}
     */
    U getId();

    /**
     * Set the identifier
     *
     * @param id identifier {@link U}
     */
    void setId(U id);
}
