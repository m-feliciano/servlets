package com.dev.servlet.web.service;

import com.dev.servlet.web.scrape.IWebScrapeService;
import com.dev.servlet.web.scrape.WebScrapeServiceEnum;

import java.util.Optional;

/**
 * Web scrape engine that delegates to specific services.
 */
@SuppressWarnings("unchecked")
public class WebScrapeService<T> {

    private final WebScrapeRequest webScrapeRequest;

    public WebScrapeService(WebScrapeRequest webScrapeRequest) {
        this.webScrapeRequest = webScrapeRequest;
    }

    public Optional<T> run() throws Exception {
        WebScrapeServiceEnum webScrapeServiceEnum = webScrapeRequest.getWebScrapeServiceEnum();
        IWebScrapeService<T> webScrapeService = (IWebScrapeService<T>) webScrapeServiceEnum.getWebScrapeService();

        if (webScrapeService == null) {
            throw new IllegalArgumentException("No web scrape service found for: " + webScrapeServiceEnum.getName());
        }

        return webScrapeService.scrape(this.webScrapeRequest);
    }
}