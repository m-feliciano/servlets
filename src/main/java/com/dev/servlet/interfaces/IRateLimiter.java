package com.dev.servlet.interfaces;

public interface IRateLimiter {

    /**
     * Acquire a token.
     *
     * @return true if a token was acquired, false otherwise
     */
    boolean acquire();

    /**
     * Acquire a token or wait for the specified time.
     *
     * @param milliseconds the time to wait
     * @return true if a token was acquired, false otherwise
     */
    boolean acquireOrWait(int milliseconds);
}
