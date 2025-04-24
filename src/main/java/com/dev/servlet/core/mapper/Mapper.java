package com.dev.servlet.core.mapper;

/**
 * Functional interface for mapping objects from one type to another.
 * 
 * <p>This interface provides a contract for transformation operations between different
 * object types, commonly used for converting between entities and DTOs, or other
 * data transformation scenarios. It follows the functional programming paradigm
 * and can be used with lambda expressions or method references.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Mapper<User, UserDTO> userMapper = user -> new UserDTO(user.getName(), user.getEmail());
 * UserDTO dto = userMapper.map(user);
 * }</pre>
 * 
 * @param <R> the source type to map from
 * @param <U> the target type to map to
 * @author servlets-team
 * @since 1.0
 */
@FunctionalInterface
public interface Mapper<R, U> {
    
    /**
     * Maps an object from the source type to the target type.
     *
     * @param object the source object to be mapped
     * @return the mapped object of the target type
     */
    U map(R object);
}
