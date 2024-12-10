package com.dev.servlet.pojo.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum StatusEnum {
    ACTIVE(1, "A"),
    DELETED(2, "X");

    private final int code;
    private final String value;

    StatusEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public static StatusEnum from(int cod) {
        return Arrays.stream(StatusEnum.values())
                .filter(id -> id != null && id.code == cod)
                .findFirst()
                .orElse(null);
    }
}
