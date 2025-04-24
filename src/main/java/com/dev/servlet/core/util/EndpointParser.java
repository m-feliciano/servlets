package com.dev.servlet.core.util;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;

/**
 * URL endpoint parser utility for extracting routing information from HTTP request paths.
 * This class parses structured URL patterns to extract controller names, API versions,
 * and endpoint paths for request dispatching and routing.
 * 
 * <p>Expected URL structure:
 * <pre>
 * /{app}/{api}/{version}/{controller}/{endpoint...}
 * </pre>
 * 
 * <p>Examples of parsed URLs:
 * <ul>
 *   <li>{@code /app/api/v1/user/profile} → controller: "UserController", version: "v1", endpoint: "profile"</li>
 *   <li>{@code /app/api/v2/product/search/category} → controller: "ProductController", version: "v2", endpoint: "search/category"</li>
 *   <li>{@code /servlet/api/v1/order/123/items} → controller: "OrderController", version: "v1", endpoint: "123/items"</li>
 * </ul>
 * 
 * <p>Key features:
 * <ul>
 *   <li><strong>Controller resolution:</strong> Automatically capitalizes and appends "Controller" suffix</li>
 *   <li><strong>API versioning:</strong> Extracts version information for API compatibility</li>
 *   <li><strong>Nested endpoints:</strong> Supports multi-level endpoint paths</li>
 *   <li><strong>Validation:</strong> Ensures URL format compliance</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Parse a request URL
 * String requestPath = "/servlet/api/v1/user/profile/settings";
 * EndpointParser parser = EndpointParser.of(requestPath);
 * 
 * // Extract routing information
 * String controller = parser.getController();  // "UserController"
 * String version = parser.getApiVersion();     // "v1"
 * String endpoint = parser.getEndpoint();      // "profile/settings"
 * 
 * // Use for request dispatching
 * Object controllerBean = BeanUtil.getResolver().getService(controller);
 * // Route to appropriate handler method based on endpoint
 * }
 * </pre>
 * 
 * <p><strong>URL Format Requirements:</strong>
 * <ul>
 *   <li>Minimum 4 path segments required</li>
 *   <li>Third segment must be API version (e.g., "v1", "v2")</li>
 *   <li>Fourth segment becomes controller name</li>
 *   <li>Remaining segments form the endpoint path</li>
 * </ul>
 * 
 * @since 1.0
 * @see BeanUtil
 */
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class EndpointParser {
    
    /** Index position of API version in URL path segments */
    private static final int API_VERSION_INDEX = 2;
    
    /** Starting index for service/endpoint name segments */
    private static final int SERVICE_NAME_START_INDEX = 4;
    
    /** The resolved controller name with "Controller" suffix */
    private String controller;
    
    /** The API version extracted from the URL (e.g., "v1", "v2") */
    private String apiVersion;
    
    /** The endpoint path after the controller name */
    private String endpoint;

    /**
     * Private constructor for parsing an endpoint URL.
     * Validates the URL format and extracts routing components.
     * 
     * @param endpoint the URL path to parse
     * @throws IllegalArgumentException if URL is null, empty, or has invalid format
     */
    private EndpointParser(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalArgumentException("Request or endpoint cannot be null or empty");
        }
        String[] parts = endpoint.split("/");
        if (parts.length < SERVICE_NAME_START_INDEX) {
            throw new IllegalArgumentException("Invalid endpoint format: " + endpoint);
        }
        init(parts);
    }

    /**
     * Factory method to create an EndpointParser instance from a URL path.
     * This is the primary entry point for parsing endpoint URLs.
     * 
     * @param path the URL path to parse (e.g., "/app/api/v1/user/profile")
     * @return configured EndpointParser instance with extracted routing information
     * @throws IllegalArgumentException if path format is invalid
     */
    public static EndpointParser of(String path) {
        return new EndpointParser(path);
    }

    /**
     * Initializes the parser fields from the split URL path segments.
     * Extracts and formats the controller name, API version, and endpoint path.
     * 
     * @param parts the URL path split by "/" delimiter
     */
    private void init(String[] parts) {
        this.apiVersion = parts[API_VERSION_INDEX];
        this.controller = StringUtils.capitalize(parts[3]).concat("Controller");
        this.endpoint = String.join("/",
                Arrays.copyOfRange(parts, SERVICE_NAME_START_INDEX, parts.length));
    }
}
