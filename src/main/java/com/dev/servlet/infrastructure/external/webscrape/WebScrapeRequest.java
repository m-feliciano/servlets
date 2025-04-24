package com.dev.servlet.infrastructure.external.webscrape;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Map;

@Getter
@AllArgsConstructor
public class WebScrapeRequest {
    private final String serviceType;
    private final String url;
    private final Map<String, Object> params;
}
