package com.dev.servlet.application.transfer.records;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Sort implements Serializable {

    private final String field;
    private Direction direction;

    private Sort(String field) {
        this.field = field;
        this.direction = Direction.UNSET;
    }

    public static Sort unsorted() {
        return new Sort(null);
    }

    public static Sort by(String name) {
        return new Sort(name);
    }

    public Sort direction(Direction direction) {
        this.setDirection(direction);
        return this;
    }

    public Sort ascending() {
        this.direction = Direction.ASC;
        return this;
    }

    public Sort descending() {
        this.direction = Direction.DESC;
        return this;
    }

    public enum Direction {
        ASC("asc"),
        DESC("desc"),
        UNSET(null);

        @Getter
        private final String value;

        Direction(String value) {
            this.value = value;
        }

        public static Direction from(String value) {
            for (Direction direction : Direction.values()) {
                if (direction.value.equalsIgnoreCase(value)) {
                    return direction;
                }
            }
            return null;
        }
    }
}

