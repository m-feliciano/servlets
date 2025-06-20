package com.dev.servlet.application.transfer.records;


import lombok.Getter;

/**
 * This class represents a key pair
 *
 * @since 1.4
 */
public record KeyPair(@Getter String key, @Getter Object value) {
}
