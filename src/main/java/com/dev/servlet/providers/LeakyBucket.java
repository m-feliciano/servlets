package com.dev.servlet.providers;

import com.dev.servlet.interfaces.IRateLimiter;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class implements the Leaky Bucket algorithm for rate limiting.
 */
@Singleton
public class LeakyBucket implements IRateLimiter {

    private static final int MAX_REQUESTS = 10; // 10 requests per second
    private static final long REFILL_TIME = 1000; // 1 second
    private static final int REFILL_AMOUNT = 5; // refill 5 tokens per second

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition tokensAvailable = lock.newCondition();
    private final AtomicInteger availableTokens = new AtomicInteger(MAX_REQUESTS);
    private long lastRefillTime = System.currentTimeMillis();

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