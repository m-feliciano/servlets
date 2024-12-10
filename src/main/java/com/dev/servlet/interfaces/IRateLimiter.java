package com.dev.servlet.interfaces;

public interface IRateLimiter {

    boolean acquire();
    boolean acquireOrWait(int milliseconds);
}
