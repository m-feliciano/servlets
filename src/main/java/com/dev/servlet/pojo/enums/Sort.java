package com.dev.servlet.pojo.enums;

import lombok.Getter;

@Getter
public enum Sort {
    ID("id"),
    NAME("name"),
    DESCRIPTION("description"),
    ;

    private final String value;

    Sort(String value) {
        this.value = value;
    }

    public static Sort from(String value) {
        for (Sort sort : Sort.values()) {
            if (sort.value.equalsIgnoreCase(value)) {
                return sort;
            }
        }
        return null;
    }
}
