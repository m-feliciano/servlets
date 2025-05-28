package com.dev.servlet.util;

import com.dev.servlet.exception.ServiceException;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ThrowableUtils {

    public static void throwIfTrue(boolean shouldThrow, int statusCode, String message)
            throws ServiceException {

        if (shouldThrow) {
            throw new ServiceException(statusCode, message);
        }
    }
}
