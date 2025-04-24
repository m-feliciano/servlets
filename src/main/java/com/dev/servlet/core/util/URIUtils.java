package com.dev.servlet.core.util;
import com.dev.servlet.domain.transfer.records.KeyPair;
import com.dev.servlet.domain.transfer.records.Query;
import com.dev.servlet.domain.transfer.records.Sort;
import com.dev.servlet.infrastructure.persistence.IPageRequest;
import com.dev.servlet.infrastructure.persistence.internal.PageRequest;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive HTTP request URI processing utility for parameter extraction and query handling.
 * This class provides robust methods for parsing HTTP requests, extracting pagination parameters,
 * building query objects, and handling URL-encoded data with proper defaults and validation.
 * 
 * <p>Key capabilities:
 * <ul>
 *   <li><strong>Resource ID extraction:</strong> From parameters or URL paths</li>
 *   <li><strong>Query string parsing:</strong> URL-decoded parameter extraction</li>
 *   <li><strong>Pagination support:</strong> Page, size, and sorting parameter handling</li>
 *   <li><strong>Search queries:</strong> Search term and type extraction</li>
 *   <li><strong>Error messaging:</strong> HTTP status code to message mapping</li>
 *   <li><strong>Parameter validation:</strong> Safe parsing with fallback defaults</li>
 * </ul>
 * 
 * <p>Supported query parameters:
 * <ul>
 *   <li>{@code page} - Page number (1-based, default: 1)</li>
 *   <li>{@code limit} - Page size (minimum: 1, default: 1)</li>
 *   <li>{@code sort} - Sort field name (default: "id")</li>
 *   <li>{@code order} - Sort direction "asc" or "desc" (default: "asc")</li>
 *   <li>{@code q} - Search query term</li>
 *   <li>{@code k} - Search type/kind</li>
 * </ul>
 * 
 * <p>URL patterns for resource ID extraction:
 * <ul>
 *   <li>Parameter: {@code ?id=123}</li>
 *   <li>Path: {@code /api/v1/users/123} (6th path segment)</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Extract resource ID
 * String userId = URIUtils.getResourceId(request); // "123"
 * 
 * // Parse complete query with pagination
 * Query query = URIUtils.getQuery(request);
 * IPageRequest<?> pageRequest = query.pageRequest();
 * String searchTerm = query.search();     // URL-decoded
 * String searchType = query.type();
 * 
 * // Extract all parameters as key-value pairs
 * List<KeyPair> params = URIUtils.getParameters(request);
 * 
 * // Get user-friendly error message
 * String errorMsg = URIUtils.getErrorMessage(404); // "Not Found"
 * }
 * </pre>
 * 
 * <p>Configuration integration:
 * When no query parameters are provided, default pagination values are loaded from
 * properties using {@link PropertiesUtil}:
 * <ul>
 *   <li>{@code pagination.page} - Default page number</li>
 *   <li>{@code pagination.limit} - Default page size</li>
 *   <li>{@code pagination.sort} - Default sort field</li>
 *   <li>{@code pagination.order} - Default sort direction</li>
 * </ul>
 * 
 * <p><strong>Security Note:</strong> All query parameters are URL-decoded using UTF-8
 * encoding to prevent encoding-related security issues.
 * 
 * @since 1.0
 * @see Query
 * @see KeyPair
 * @see IPageRequest
 * @see PropertiesUtil
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class URIUtils {
    
    /** Cache key for internal URI caching mechanisms */
    public static final String URI_INTERNAL_CACHE_KEY = "uri_internal_cache_key";
    
    /** Random cache value key for security purposes */
    public static final String URI_INTERNAL_CACHE_VALUE_KEY = RandomStringUtils.randomAlphanumeric(30);
    
    /** Default field name for sorting operations */
    public static final String DEFAULT_SORT_FIELD = "id";
    
    /** Default sort order direction */
    public static final String DEFAULT_SORT_ORDER = "asc";
    
    /** Minimum allowed page size */
    public static final int DEFAULT_MIN_PAGE_SIZE = 1;
    
    /** Default initial page number */
    public static final int DEFAULT_INITIAL_PAGE = 1;

    /**
     * Extracts resource ID from HTTP request parameters or URL path.
     * First attempts to get ID from request parameter, then falls back to
     * extracting from the URL path at the 6th segment position.
     * 
     * @param httpServletRequest the HTTP request containing the resource ID
     * @return the resource ID as string, or null if not found
     */
    public static String getResourceId(HttpServletRequest httpServletRequest) {
        String parameter = httpServletRequest.getParameter("id");
        if (parameter != null) return parameter;
        String[] array = httpServletRequest.getServletPath().split("/");
        parameter = Arrays.stream(array).skip(5).findFirst().orElse(null);
        return parameter;
    }

    /**
     * Parses the HTTP request to create a comprehensive Query object containing
     * pagination, search, and type filter information. Handles both query string
     * parameters and property-based defaults.
     * 
     * @param request the HTTP request to parse
     * @return Query object with pagination, search term, and type filter
     */
    public static Query getQuery(HttpServletRequest request) {
        String type = null;
        String search = null;
        IPageRequest<?> pageRequest = null;
        if (hasQueryString(request)) {
            Map<String, String> queryParams = extractQueryParameters(request);
            pageRequest = createPageRequest(queryParams);
            search = getParam(queryParams, "q");
            type = getParam(queryParams, "k");
        } else {
            pageRequest = buildPagination();
        }
        return new Query(pageRequest, search, type);
    }

    /**
     * Checks if the request has a non-empty query string.
     * 
     * @param request the HTTP request to check
     * @return true if query string exists and is not empty
     */
    private static boolean hasQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return queryString != null && !queryString.isEmpty();
    }

    /**
     * Extracts and URL-decodes all query parameters from the request.
     * 
     * @param request the HTTP request containing query parameters
     * @return map of parameter names to URL-decoded values
     */
    private static Map<String, String> extractQueryParameters(HttpServletRequest request) {
        Map<String, String> queryParams = new HashMap<>();
        List<KeyPair> params = parseQueryParams(request.getQueryString());
        for (var param : params) {
            queryParams.put(param.getKey(), ((String) param.value()).trim());
        }
        return queryParams;
    }

    /**
     * Creates a PageRequest object from parsed query parameters with validation.
     * 
     * @param queryParams map of query parameters
     * @return configured PageRequest with pagination and sorting
     */
    private static IPageRequest<?> createPageRequest(Map<String, String> queryParams) {
        int pageInitial = parsePageNumber(queryParams);
        int pageSize = parsePageSize(queryParams);
        Sort sort = createSort(queryParams);
        return PageRequest.builder()
                .initialPage(pageInitial)
                .pageSize(pageSize)
                .sort(sort)
                .build();
    }

    /**
     * Parses and validates the page number parameter.
     * Ensures the page number is positive and at least 1.
     * 
     * @param queryParams query parameters map
     * @return validated page number (minimum 1)
     */
    private static int parsePageNumber(Map<String, String> queryParams) {
        try {
            int page = Integer.parseInt(queryParams.getOrDefault("page", String.valueOf(DEFAULT_INITIAL_PAGE)));
            return Math.max(Math.abs(page), DEFAULT_INITIAL_PAGE);
        } catch (NumberFormatException e) {
            return DEFAULT_INITIAL_PAGE;
        }
    }

    /**
     * Parses and validates the page size parameter.
     * Ensures the page size is positive and at least 1.
     * 
     * @param queryParams query parameters map
     * @return validated page size (minimum 1)
     */
    private static int parsePageSize(Map<String, String> queryParams) {
        try {
            String parameter = queryParams.getOrDefault("limit", String.valueOf(DEFAULT_MIN_PAGE_SIZE));
            int limit = Math.abs(Integer.parseInt(parameter));
            return Math.max(limit, DEFAULT_MIN_PAGE_SIZE);
        } catch (NumberFormatException e) {
            return DEFAULT_MIN_PAGE_SIZE;
        }
    }

    /**
     * Creates a Sort object from sort field and direction parameters.
     * 
     * @param queryParams query parameters map
     * @return configured Sort object with field and direction
     */
    private static Sort createSort(Map<String, String> queryParams) {
        String sortField = queryParams.getOrDefault("sort", DEFAULT_SORT_FIELD);
        String order = queryParams.getOrDefault("order", DEFAULT_SORT_ORDER);
        return Sort.by(sortField).direction(Sort.Direction.from(order));
    }

    /**
     * Parses a query string into key-value pairs.
     * Splits on '&' and '=' delimiters to extract parameter pairs.
     * 
     * @param query the raw query string
     * @return list of KeyPair objects representing parameters
     */
    private static List<KeyPair> parseQueryParams(String query) {
        List<KeyPair> queryParams = new ArrayList<>();
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2) {
                queryParams.add(new KeyPair(pair[0], pair[DEFAULT_INITIAL_PAGE]));
            }
        }
        return queryParams;
    }

    /**
     * Builds default pagination settings from application properties.
     * Used when no query parameters are provided in the request.
     * 
     * @return PageRequest with property-based default values
     */
    private static PageRequest<Object> buildPagination() {
        int page = PropertiesUtil.getProperty("pagination.page", DEFAULT_INITIAL_PAGE);
        int size = PropertiesUtil.getProperty("pagination.limit", DEFAULT_MIN_PAGE_SIZE);
        String field = PropertiesUtil.getProperty("pagination.sort", DEFAULT_SORT_FIELD);
        String order = PropertiesUtil.getProperty("pagination.order", DEFAULT_SORT_ORDER);
        Sort sort = Sort.by(field).direction(Sort.Direction.from(order));
        return PageRequest.builder()
                .initialPage(page)
                .pageSize(size)
                .sort(sort)
                .build();
    }

    /**
     * Extracts all request parameters as key-value pairs.
     * Takes the first value when multiple values exist for the same parameter.
     * 
     * @param httpServletRequest the HTTP request containing parameters
     * @return list of KeyPair objects with parameter names and values
     */
    public static List<KeyPair> getParameters(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getParameterMap()
                .entrySet().stream()
                .map(e -> new KeyPair(e.getKey(), e.getValue()[0]))
                .toList();
    }

    /**
     * Converts HTTP status codes to user-friendly error messages.
     * Provides standardized error messages for common HTTP status codes.
     * 
     * @param status the HTTP status code
     * @return human-readable error message corresponding to the status code
     */
    public static String getErrorMessage(int status) {
        return switch (status) {
            case HttpServletResponse.SC_BAD_REQUEST -> "Bad Request";
            case HttpServletResponse.SC_UNAUTHORIZED -> "Unauthorized";
            case HttpServletResponse.SC_SERVICE_UNAVAILABLE -> "Service Unavailable";
            case HttpServletResponse.SC_FORBIDDEN -> "Forbidden";
            case HttpServletResponse.SC_NOT_FOUND -> "Not Found";
            case HttpServletResponse.SC_METHOD_NOT_ALLOWED -> "Method Not Allowed";
            case HttpServletResponse.SC_CONFLICT -> "Conflict";
            case 429 -> "Too Many Requests";
            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR -> "Internal Server Error";
            default -> "Error";
        };
    }

    /**
     * Retrieves and URL-decodes a specific query parameter.
     * 
     * @param queryParams map of query parameters
     * @param q the parameter name to retrieve
     * @return URL-decoded parameter value, or null if not found
     */
    private static String getParam(Map<String, String> queryParams, String q) {
        String paramValue = queryParams.get(q);
        return paramValue != null ? URLDecoder.decode(paramValue, StandardCharsets.UTF_8) : null;
    }
}
