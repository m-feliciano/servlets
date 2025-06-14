package com.dev.servlet.domain.model.pojo.enums;


import lombok.Getter;

@Getter
public enum RoleType {
    ADMIN(1L, "ADMIN"),
    DEFAULT(2L, "USER"),
    MODERATOR(3L, "MODERATOR"),
    VISITOR(4L, "GUEST");

    private final Long code;
    private final String description;

    RoleType(Long code, String descricao) {
        this.code = code;
        this.description = descricao;
    }

    /**
     * Gets code.
     *
     * @return the enum value
     */
    public static RoleType toEnum(Long code) {
        if (code == null) return null;

        for (RoleType p : RoleType.values()) {
            if (code.equals(p.code))
                return p;
        }

        throw new IllegalArgumentException("Invalid Id: " + code);
    }
}

