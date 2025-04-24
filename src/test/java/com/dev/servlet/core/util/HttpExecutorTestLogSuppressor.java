package com.dev.servlet.core.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpExecutorTestLogSuppressor implements BeforeAllCallback, AfterAllCallback {
    private static final Map<String, Level> originalLevels = new HashMap<>();
    private static final String[] LOGGERS_TO_SUPPRESS = {
            "com.dev.servlet.core.exception.ServiceException",
            "com.dev.servlet.adapter.internal.HttpExecutor"
    };

    @Override
    public void beforeAll(ExtensionContext context) {
        for (String loggerName : LOGGERS_TO_SUPPRESS) {
            Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
            originalLevels.put(loggerName, logger.getLevel());
            logger.setLevel(Level.OFF);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        for (String loggerName : LOGGERS_TO_SUPPRESS) {
            Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
            logger.setLevel(originalLevels.getOrDefault(loggerName, Level.INFO));
        }
    }
}