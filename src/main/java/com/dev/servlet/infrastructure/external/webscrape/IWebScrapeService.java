package com.dev.servlet.infrastructure.external.webscrape;

import java.util.Optional;

/**
 * Generic web scrape service.
 */
public interface IWebScrapeService<TResponse> {

    /**
     * Scrape data based on the provided request.
     *
     * @param request the web scrape request containing parameters and URL
     * @return an Optional containing the scraped data, or empty if not found
     * @throws Exception if an error occurs during scraping
     */
    Optional<TResponse> scrape(WebScrapeRequest request) throws Exception;
}