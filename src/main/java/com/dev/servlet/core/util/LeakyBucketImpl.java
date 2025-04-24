package com.dev.servlet.core.util;

import com.dev.servlet.core.interfaces.IRateLimiter;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe implementation of the Leaky Bucket rate limiting algorithm.
 * This class provides controlled request throttling using a bucket metaphor where
 * tokens represent request permissions that leak out at a constant rate and refill periodically.
 * 
 * <p>Algorithm characteristics:
 * <ul>
 *   <li><strong>Bucket capacity:</strong> 10 tokens maximum</li>
 *   <li><strong>Refill rate:</strong> 5 tokens every 1000ms (1 second)</li>
 *   <li><strong>Token consumption:</strong> 1 token per request</li>
 *   <li><strong>Overflow behavior:</strong> Requests denied when bucket is empty</li>
 * </ul>
 * 
 * <p>The leaky bucket algorithm provides:
 * <ul>
 *   <li><strong>Burst handling:</strong> Allows short bursts up to bucket capacity</li>
 *   <li><strong>Smooth traffic:</strong> Maintains steady-state rate limiting</li>
 *   <li><strong>Memory efficiency:</strong> Constant memory usage regardless of traffic</li>
 *   <li><strong>Fairness:</strong> First-come-first-served token allocation</li>
 * </ul>
 * 
 * <p>Thread safety features:
 * <ul>
 *   <li>ReentrantLock for exclusive access to critical sections</li>
 *   <li>AtomicInteger for lock-free token counting</li>
 *   <li>Condition variables for efficient waiting</li>
 *   <li>Safe concurrent access from multiple threads</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @Inject
 * private IRateLimiter rateLimiter;
 * 
 * // In request handler
 * public void handleRequest() {
 *     if (!rateLimiter.acquire()) {
 *         throw new ServiceException(429, "Rate limit exceeded");
 *     }
 *     // Process request
 * }
 * 
 * // With timeout waiting
 * public void handleRequestWithWait() {
 *     if (!rateLimiter.acquireOrWait(500)) { // Wait up to 500ms
 *         throw new ServiceException(429, "Rate limit exceeded");
 *     }
 *     // Process request
 * }
 * }
 * </pre>
 * 
 * <p>Performance characteristics:
 * <ul>
 *   <li><strong>Immediate response:</strong> O(1) token acquisition when available</li>
 *   <li><strong>Bounded waiting:</strong> Configurable timeout for token availability</li>
 *   <li><strong>Low overhead:</strong> Minimal CPU and memory usage</li>
 *   <li><strong>High throughput:</strong> Supports concurrent access patterns</li>
 * </ul>
 * 
 * <p>Rate limiting behavior:
 * <ul>
 *   <li>Initial burst: 10 requests immediately</li>
 *   <li>Sustained rate: 5 requests per second</li>
 *   <li>Recovery time: 2 seconds to fully refill empty bucket</li>
 * </ul>
 * 
 * @since 1.0
 * @see IRateLimiter
 */
@Singleton
public class LeakyBucketImpl implements IRateLimiter {
    
    /** Maximum number of tokens the bucket can hold */
    private static final int MAX_REQUESTS = 10;
    
    /** Time interval between refill operations in milliseconds */
    private static final long REFILL_TIME = 1000;
    
    /** Number of tokens added during each refill operation */
    private static final int REFILL_AMOUNT = 5;
    
    /** Lock for thread-safe access to bucket state */
    private final ReentrantLock lock = new ReentrantLock();
    
    /** Condition variable for waiting threads when tokens become available */
    private final Condition tokensAvailable = lock.newCondition();
    
    /** Current number of available tokens in the bucket */
    private final AtomicInteger availableTokens = new AtomicInteger(MAX_REQUESTS);
    
    /** Timestamp of the last refill operation */
    private long lastRefillTime = System.currentTimeMillis();
    
    /**
     * Attempts to acquire a token from the bucket immediately.
     * This method does not block and returns immediately whether
     * a token was successfully acquired or not.
     * 
     * <p>The method performs these operations atomically:
     * <ol>
     *   <li>Refill tokens based on elapsed time</li>
     *   <li>Check token availability</li>
     *   <li>Consume one token if available</li>
     * </ol>
     * 
     * @return true if a token was successfully acquired, false if bucket is empty
     */
    @Override
    public boolean acquire() {
        lock.lock();
        try {
            refill();
            if (availableTokens.get() > 0) {
                availableTokens.decrementAndGet();
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Refills the bucket with tokens based on elapsed time since last refill.
     * This method calculates how many refill periods have passed and adds
     * the appropriate number of tokens, capped at the maximum bucket capacity.
     * 
     * <p>The refill logic:
     * <ul>
     *   <li>Calculate elapsed time since last refill</li>
     *   <li>Determine number of complete refill periods</li>
     *   <li>Add tokens proportional to elapsed periods</li>
     *   <li>Cap total tokens at maximum bucket capacity</li>
     * </ul>
     */
    private void refill() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRefillTime;
        if (elapsedTime >= REFILL_TIME) {
            int tokensToAdd = (int) (elapsedTime / REFILL_TIME) * REFILL_AMOUNT;
            availableTokens.set(Math.min(availableTokens.get() + tokensToAdd, MAX_REQUESTS));
            lastRefillTime = currentTime;
            tokensAvailable.signalAll();
        }
    }
    
    /**
     * Attempts to acquire a token, waiting up to the specified timeout if none are available.
     * This method provides blocking behavior with a configurable timeout for scenarios
     * where waiting for token availability is acceptable.
     * 
     * <p>Waiting behavior:
     * <ul>
     *   <li>First attempts immediate acquisition</li>
     *   <li>If unsuccessful, waits for tokens to become available</li>
     *   <li>Periodically checks for token availability during wait</li>
     *   <li>Returns false if timeout expires without acquiring token</li>
     * </ul>
     * 
     * <p>The method respects thread interruption and will return false
     * if the waiting thread is interrupted.
     * 
     * @param milliseconds maximum time to wait for a token in milliseconds
     * @return true if a token was acquired within the timeout, false otherwise
     */
    @Override
    public boolean acquireOrWait(int milliseconds) {
        lock.lock();
        try {
            if (acquire()) {
                return true;
            }
            long endTime = System.currentTimeMillis() + milliseconds;
            while (System.currentTimeMillis() < endTime) {
                long remainingTime = endTime - System.currentTimeMillis();
                if (remainingTime <= 0) {
                    break;
                }
                try {
                    tokensAvailable.awaitNanos(remainingTime * 1_000_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                if (acquire()) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Demonstration main method showing rate limiter behavior.
     * This method is included for testing and educational purposes,
     * demonstrating both immediate and waiting acquisition patterns.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        IRateLimiter leakyBucket = new LeakyBucketImpl();
        for (int i = 0; i < 20; i++) {
            System.out.println(leakyBucket.acquire());
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        int milliseconds = 200;
        for (int i = 0; i < 20; i++) {
            System.out.println(leakyBucket.acquireOrWait(milliseconds));
        }
    }
}
