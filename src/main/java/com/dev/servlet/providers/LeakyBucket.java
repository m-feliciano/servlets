package com.dev.servlet.providers;

import com.dev.servlet.interfaces.IRateLimiter;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class implements the Leaky Bucket algorithm for rate limiting.
 *
 * @author marcelo.feliciano
 */
@Singleton
public class LeakyBucket implements IRateLimiter {

    private static final int MAX_REQUESTS = 10; // 10 requests per second
    private static final long REFILL_TIME = 1000; // 1 second
    private static final int REFILL_AMOUNT = 5; // refill 5 tokens per second

    private static final ReentrantLock lock;
    private static final Condition tokensAvailable;
    private static final AtomicReference<Integer> availableTokens;
    private static final AtomicReference<Long> lastRefillTime;


    static {
        lock = new ReentrantLock();
        tokensAvailable = lock.newCondition();
        availableTokens = new AtomicReference<>(MAX_REQUESTS);
        lastRefillTime = new AtomicReference<>(System.currentTimeMillis());
    }

    public LeakyBucket() {
        // Empty constructor
    }

    /**
     * Acquires a token if available.
     *
     * @return true if a token is acquired, false otherwise
     * @author marcelo.feliciano
     */
    @Override
    public boolean acquire() {
        lock.lock();
        try {
            refill();
            if (availableTokens.get() > 0) {
                availableTokens.set(availableTokens.get() - 1);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    private void refill() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRefillTime.get();

        if (elapsedTime >= REFILL_TIME) {
            // Calculate the number of tokens to add based on the elapsed time
            int tokensToAdd = (int) (elapsedTime / REFILL_TIME) * REFILL_AMOUNT;
            availableTokens.set(Math.min(availableTokens.get() + tokensToAdd, MAX_REQUESTS));
            lastRefillTime.set(currentTime);

            lock.lock(); // Acquire the lock to signal waiting threads
            try {
                tokensAvailable.signalAll(); // Signal all waiting threads that token is available
            } finally {
                lock.unlock(); // Release the lock
            }
        }
    }

    /**
     * Acquires a token or waits for the specified time.
     *
     * @param milliseconds
     * @return
     * @author marcelo.feliciano
     */
    @Override
    public boolean acquireOrWait(int milliseconds) {
        lock.lock();
        try {
            if (acquire()) { // Try to acquire a token immediately
                return true; // If successful, return true
            }

            long endTime = System.currentTimeMillis() + milliseconds;
            while (System.currentTimeMillis() < endTime) { // Loop until the end time is reached
                long remainingTime = endTime - System.currentTimeMillis();
                if (remainingTime <= 0) {
                    break;
                }
                try {
                    // Wait for tokens to become available or until the remaining time elapses
                    long awaited = tokensAvailable.awaitNanos(remainingTime * 1_000_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                    return false; // Return false if interrupted
                }

                if (acquire()) { // Try to acquire a token again after waiting
                    return true;
                }
            }
            return false; // Return false if unable to acquire a token within the specified time
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        IRateLimiter leakyBucket = new LeakyBucket();
        for (int i = 0; i < 20; i++) {
            System.out.println(leakyBucket.acquire());
        }

        // Wait for 1 second
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Test acquireOrWait
        int milliseconds = 200;
        for (int i = 0; i < 20; i++) {
            System.out.println(leakyBucket.acquireOrWait(milliseconds));
        }
    }
}