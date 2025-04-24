package com.dev.servlet.domain.model.enums;
import lombok.Getter;
import java.util.Arrays;

@Getter
public enum Status {
    ACTIVE(1, "A"),
    DELETED(2, "X");
    private final int code;
    private final String value;
    Status(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public static Status from(int cod) {
        return Arrays.stream(Status.values())
                .filter(id -> id != null && id.code == cod)
                .findFirst()
                .orElse(null);
    }
}
