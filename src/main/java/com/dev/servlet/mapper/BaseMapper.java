package com.dev.servlet.mapper;

import com.dev.servlet.interfaces.IMapper;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public abstract class BaseMapper<T, J> implements IMapper<T, J> {

    /**
     * Convert a list of entities to a list of DTOs.
     *
     * @param entities List of entities {@link T}
     * @return List of DTOs {@link J}
     */
    public List<J> fromEntities(List<T> entities) {
        return entities.stream().map(this::fromEntity).toList();
    }

    /**
     * Convert a list of DTOs to a list of entities.
     *
     * @param dtos List of DTOs {@link J}
     * @return List of entities {@link T}
     */
    public List<T> toEntities(List<J> dtos) {
        return dtos.stream().map(this::toEntity).toList();
    }
}
