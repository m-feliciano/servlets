package com.dev.servlet.infrastructure.external.webscrape;
import com.dev.servlet.infrastructure.external.webscrape.service.ProductWebScrapeApiClient;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class WebScrapeServiceRegistry {
    private final Map<String, IWebScrapeService<?>> registry = new HashMap<>();
    public WebScrapeServiceRegistry() {
        registerService("product", new ProductWebScrapeApiClient());
    }

    public IWebScrapeService<?> getService(String type) {
        return registry.get(type);
    }

    public void registerService(String type, IWebScrapeService<?> service) {
        registry.put(type, service);
    }
}
