package com.dev.servlet.core.interfaces;

/**
 * Interface for implementing rate limiting functionality to control request frequency.
 * 
 * <p>This interface provides methods for acquiring permissions to proceed with operations
 * based on rate limiting policies. It supports both immediate acquisition attempts and
 * waiting strategies to handle rate limiting in different scenarios. Rate limiting is
 * essential for protecting system resources and ensuring fair usage across clients.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * IRateLimiter rateLimiter = new TokenBucketRateLimiter(10); // 10 requests per second
 * if (rateLimiter.acquire()) {
 *     // Process request
 * } else {
 *     // Handle rate limit exceeded
 * }
 * }</pre>
 * 
 * @author servlets-team
 * @since 1.0
 */
public interface IRateLimiter {
    
    /**
     * Attempts to acquire a permit to proceed with an operation.
     * This method returns immediately without waiting.
     *
     * @return true if a permit was successfully acquired, false if rate limit is exceeded
     */
    boolean acquire();
    
    /**
     * Attempts to acquire a permit, waiting up to the specified time if necessary.
     * This method may block the calling thread if no permit is immediately available.
     *
     * @param milliseconds the maximum time to wait for a permit in milliseconds
     * @return true if a permit was acquired within the timeout, false otherwise
     */
    boolean acquireOrWait(int milliseconds);
}
