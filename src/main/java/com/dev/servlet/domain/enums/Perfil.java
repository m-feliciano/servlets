package com.dev.servlet.domain.enums;

public enum Perfil {

    ADMIN(1, "ROLE_ADMIN"),
    USER(2, "ROLE_USER"),
    MODERATOR(2, "ROLE_MODERATOR");

    public final int cod;
    public final String description;

    Perfil(int cod, String descricao) {
        this.cod = cod;
        this.description = descricao;
    }

    /**
     * Gets cod.
     *
     * @return the enum value
     */
    public static Perfil toEnum(Integer cod) {
        if (cod == null) {
            return null;
        }

        for (Perfil p : Perfil.values()) {
            if (cod.equals(p.cod))
                return p;
        }
        throw new IllegalArgumentException("Invalid Id: " + cod);
    }
}
