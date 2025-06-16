package com.dev.servlet.infrastructure.external.webscrape;

import com.dev.servlet.infrastructure.external.webscrape.service.ProductWebScrapeApiClient;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for web scrape services.
 * This class manages the registration and retrieval of web scrape services by type.
 * It allows for easy extension and addition of new web scrape services.
 */
@Singleton
public class WebScrapeServiceRegistry {
    private final Map<String, IWebScrapeService<?>> registry = new HashMap<>();

    public WebScrapeServiceRegistry() {
        registerService("product", new ProductWebScrapeApiClient());
        // others...
    }

    public IWebScrapeService<?> getService(String type) {
        return registry.get(type);
    }

    public void registerService(String type, IWebScrapeService<?> service) {
        registry.put(type, service);
    }
}
