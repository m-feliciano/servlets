package com.dev.servlet.web.scrape;

import com.dev.servlet.web.service.WebScrapeRequest;

import java.util.Optional;

/**
 * Generic web scrape service.
 */
public interface IWebScrapeService<T> {

    /**
     * Scrape data based on the provided request.
     *
     * @param request the web scrape request containing parameters and URL
     * @return an Optional containing the scraped data, or empty if not found
     * @throws Exception if an error occurs during scraping
     */
    Optional<T> scrape(WebScrapeRequest request) throws Exception;
}