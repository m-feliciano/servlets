package com.dev.servlet.core.util;

import com.dev.servlet.core.interfaces.IRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IRateLimiterTest {

    private IRateLimiter leakyBucket;

    @BeforeEach
    void setUp() {
        leakyBucket = new LeakyBucketImpl();
    }

    @Test
    @DisplayName(
            "Test acquire method with a valid request. " +
            "It should return true if the token can be acquired.")
    void testAcquire_Success() {
        for (int i = 0; i < 10; i++) {
            assertTrue(leakyBucket.acquire(), "Token should be acquired");
        }
    }

    @Test
    @DisplayName(
            "Test acquire method when the limit is exceeded. " +
            "It should return false if the limit is reached.")
    void testAcquire_FailureWhenExceedingLimit() {
        for (int i = 0; i < 10; i++) {
            assertTrue(leakyBucket.acquire(), "Token should be acquired");
        }
        assertFalse(leakyBucket.acquire(), "No tokens should be available");
    }

    @Test
    @DisplayName(
            "Test acquire method after waiting for refill. " +
            "It should allow acquiring tokens after the specified wait time.")
    void testRefillAfterWait() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            assertTrue(leakyBucket.acquire(), "Token should be acquired");
        }
        assertFalse(leakyBucket.acquire(), "No tokens should be available");

        // Wait for refill
        Thread.sleep(1000);

        assertTrue(leakyBucket.acquire(), "Token should be acquired after refill");
    }

    @Test
    @DisplayName(
            "Test acquireOrWait method with a successful acquisition. " +
            "It should return true if the token can be acquired immediately.")
    void testAcquireOrWait_Success() {
        for (int i = 0; i < 10; i++) {
            assertTrue(leakyBucket.acquire(), "Token should be acquired");
        }

        // Acquire with wait
        assertTrue(leakyBucket.acquireOrWait(1000), "Token should be acquired after waiting");
    }

    @Test
    @DisplayName(
            "Test acquireOrWait method with a timeout. " +
            "It should return false if the token cannot be acquired within the specified timeout.")
    void testAcquireOrWait_FailureWhenTimeout() {
        for (int i = 0; i < 10; i++) {
            assertTrue(leakyBucket.acquire(), "Token should be acquired");
        }

        // Attempt to acquire with insufficient wait time
        assertFalse(leakyBucket.acquireOrWait(50), "Token should not be acquired within timeout");
    }

    @Test
    @DisplayName(
            "Test thread safety of LeakyBucket. " +
            "Multiple threads should be able to acquire tokens concurrently without exceeding the limit.")
    void testThreadSafety() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 15; i++) {
            executor.submit(() -> assertTrue(leakyBucket.acquireOrWait(200), "Token should be acquired in multithreaded environment"));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "All threads should complete execution");
    }
}