package com.dev.servlet.interfaces;

public interface IMapper<T, J> {

    /**
     * Mapper {@link T} from {@link J}
     */
    J fromEntity(T object);

    /**
     * Mapper {@link J} from {@link T}
     */
    T toEntity(J object);
}
