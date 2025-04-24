package com.dev.servlet.infrastructure.external.webscrape;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class WebScrapeService<T> {
    private final WebScrapeServiceRegistry registry;
    public WebScrapeService(WebScrapeServiceRegistry registry) {
        this.registry = registry;
    }

    public Optional<T> run(WebScrapeRequest request) throws Exception {
        var service = (IWebScrapeService<T>) registry.getService(request.getServiceType());
        Objects.requireNonNull(service, "Service not found for type: " + request.getServiceType());
        return service.scrape(request);
    }
}
