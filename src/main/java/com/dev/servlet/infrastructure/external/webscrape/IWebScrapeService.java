package com.dev.servlet.infrastructure.external.webscrape;
import java.util.Optional;

/**
 * Service interface for web scraping operations to extract data from external sources.
 * 
 * <p>This interface defines the contract for web scraping functionality in the infrastructure layer.
 * It provides a standardized way to extract and parse data from external websites or APIs,
 * supporting various response types through generic parameterization. The service handles
 * the complexity of web scraping while providing a clean abstraction for the domain layer.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * IWebScrapeService<List<ProductDTO>> scraper = new ProductScrapeService();
 * WebScrapeRequest request = new WebScrapeRequest("https://example.com/products");
 * Optional<List<ProductDTO>> products = scraper.scrape(request);
 * }</pre>
 * 
 * @param <TResponse> the type of data returned by the scraping operation
 * @author servlets-team
 * @since 1.0
 */
public interface IWebScrapeService<TResponse> {
    
    /**
     * Scrapes data from an external source based on the provided request parameters.
     * This method extracts and parses data from web pages or APIs, returning
     * the structured data in the specified response type.
     *
     * @param request the web scrape request containing URL, parameters, and configuration
     * @return Optional containing the scraped data if successful, empty if scraping fails
     * @throws Exception if scraping fails due to network issues, parsing errors, or other problems
     */
    Optional<TResponse> scrape(WebScrapeRequest request) throws Exception;
}
