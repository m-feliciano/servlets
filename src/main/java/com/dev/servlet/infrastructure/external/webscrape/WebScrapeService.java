package com.dev.servlet.infrastructure.external.webscrape;


import java.util.Optional;

/**
 * Web scrape engine that delegates to specific services.
 */
@SuppressWarnings("unchecked")
public class WebScrapeService<T> {

    private final WebScrapeServiceRegistry registry;

    public WebScrapeService(WebScrapeServiceRegistry registry) {
        this.registry = registry;
    }

    public Optional<T> run(WebScrapeRequest request) throws Exception {
        IWebScrapeService<T> service = (IWebScrapeService<T>) registry.getService(request.getServiceType());
        if (service == null) {
            throw new IllegalArgumentException("No service found for: " + request.getServiceType());
        }

        return service.scrape(request);
    }
}
