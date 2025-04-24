package com.dev.servlet.infrastructure.external.webscrape.builder;
import com.dev.servlet.infrastructure.external.webscrape.WebScrapeRequest;
import com.dev.servlet.infrastructure.external.webscrape.WebScrapeService;
import com.dev.servlet.infrastructure.external.webscrape.WebScrapeServiceRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebScrapeBuilder<T> {
    private String serviceType;
    private String url;
    private Map<String, Object> params;
    private WebScrapeServiceRegistry registry;
    public static <T> WebScrapeBuilder<T> create() {
        return new WebScrapeBuilder<>();
    }

    public WebScrapeBuilder<T> withServiceType(String serviceType) {
        this.serviceType = serviceType;
        return this;
    }

    public WebScrapeBuilder<T> withUrl(String url) {
        this.url = url;
        return this;
    }

    public WebScrapeBuilder<T> withParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public WebScrapeBuilder<T> withRegistry(WebScrapeServiceRegistry registry) {
        this.registry = registry;
        return this;
    }

    public Optional<T> execute() throws Exception {
        Objects.requireNonNull(registry, "WebScrapeServiceRegistry must be set");
        Objects.requireNonNull(serviceType, "WebScrapeServiceType must be set");
        Objects.requireNonNull(url, "WebScrape URL must be set");
        WebScrapeRequest request = new WebScrapeRequest(serviceType, url, params);
        WebScrapeService<T> service = new WebScrapeService<>(registry);
        return service.run(request);
    }
}
