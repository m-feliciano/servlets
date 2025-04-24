package com.dev.servlet.domain.transfer.records;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sort implements Serializable {
    private String field;
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
