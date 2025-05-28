package com.dev.servlet.util;

import com.dev.servlet.model.pojo.records.KeyPair;
import com.dev.servlet.model.pojo.records.Query;
import com.dev.servlet.model.pojo.records.Sort;
import com.dev.servlet.persistence.IPageRequest;
import com.dev.servlet.persistence.impl.PageRequestImpl;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used to extract the URI information from the request.
 *
 * @since 1.4
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class URIUtils {

    public static final String URI_INTERNAL_CACHE_KEY = "uri_internal_cache_key";
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_ORDER = "asc";
    public static final int DEFAULT_MIN_PAGE_SIZE = 2;
    public static final int DEFAULT_INITIAL_PAGE = 1;

    /**
     * Return the resource id from the request.
     * The resource id can be passed as a parameter or as part of the URI.
     *
     * @param httpServletRequest {@linkplain HttpServletRequest}
     */
    public static String getResourceId(HttpServletRequest httpServletRequest) {
        String parameter = httpServletRequest.getParameter("id");
        if (parameter != null) return parameter;

        String[] array = httpServletRequest.getServletPath().split("/");
        parameter = Arrays.stream(array).skip(5).findFirst().orElse(null);
        return parameter;
    }

    /**
     * Create the query object from the request.
     *
     * @param request {@linkplain HttpServletRequest}
     * @return {@linkplain Query}
     */
    public static Query getQuery(HttpServletRequest request) {
        HashMap<String, String> queryParams = new HashMap<>();
        IPageRequest<?> pageRequest;

        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            List<KeyPair> params = parseQueryParams(request.getQueryString());
            for (var param : params) {
                queryParams.put(param.getKey(), ((String) param.value()).trim());
            }

            int page = Math.abs(Integer.parseInt(queryParams.getOrDefault("page", String.valueOf(DEFAULT_INITIAL_PAGE))));
            int pageInitial = Math.max(page, DEFAULT_INITIAL_PAGE);

            int limit = Math.abs(Integer.parseInt(queryParams.getOrDefault("limit", String.valueOf(DEFAULT_MIN_PAGE_SIZE))));
            int pageSize = Math.max(limit, DEFAULT_MIN_PAGE_SIZE);

            String sortField = queryParams.getOrDefault("sort", DEFAULT_SORT_FIELD);
            Sort.Direction direction = Sort.Direction.from(queryParams.getOrDefault("order", DEFAULT_SORT_ORDER));

            pageRequest = PageRequestImpl.builder()
                    .initialPage(pageInitial)
                    .pageSize(pageSize)
                    .sort(Sort.by(sortField).direction(direction))
                    .build();

            String search = getParam(queryParams, "q");
            String type = getParam(queryParams, "k");

            return new Query(pageRequest, search, type);
        }

        pageRequest = getDefaultPageValue();
        return new Query(pageRequest, null, null);
    }

    /**
     * Parse the query parameters from the request.
     *
     * @param query
     * @return {@linkplain List} of {@linkplain KeyPair}
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
     * Get the default query value.
     * If the value is not found in the cache, then it will be created and stored in the cache.
     * The default values are read from the properties file.
     *
     * @return {@linkplain PageRequestImpl}
     */
    public static PageRequestImpl<?> getDefaultPageValue() {
        List<Serializable> cachedData = CacheUtil.get(URI_INTERNAL_CACHE_KEY, "default_pagination_internal");

        if (!CollectionUtils.isEmpty(cachedData)) {
            Object object = cachedData.get(0);
            return (PageRequestImpl<?>) object;
        }

        var pagination = buildPagination();

        CacheUtil.set(URI_INTERNAL_CACHE_KEY, "default_pagination_internal", List.of(pagination));

        return pagination;
    }

    /**
     * Build the pagination object.
     */
    private static PageRequestImpl<?> buildPagination() {
        int page = PropertiesUtil.getProperty("pagination.page", DEFAULT_INITIAL_PAGE);
        int size = PropertiesUtil.getProperty("pagination.limit", DEFAULT_MIN_PAGE_SIZE);
        String field = PropertiesUtil.getProperty("pagination.sort", DEFAULT_SORT_FIELD);
        String order = PropertiesUtil.getProperty("pagination.order", DEFAULT_SORT_ORDER);

        Sort sort = Sort.by(field).direction(Sort.Direction.from(order));

        return PageRequestImpl.builder()
                .initialPage(page)
                .pageSize(size)
                .sort(sort)
                .build();
    }

    /**
     * Get the parameters from the request.
     *
     * @param httpServletRequest {@linkplain HttpServletRequest}
     * @return {@linkplain List} of {@linkplain KeyPair}
     */
    public static List<KeyPair> getParameters(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getParameterMap()
                .entrySet().stream()
                .map(e -> new KeyPair(e.getKey(), e.getValue()[0]))
                .toList();
    }

    /**
     * Get the error message based on the status code.
     *
     * @param status
     * @see HttpServletResponse for the status codes
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

    private static String getParam(HashMap<String, String> queryParams, String q) {
        String paramValue = queryParams.get(q);
        return paramValue != null ? URLDecoder.decode(paramValue, StandardCharsets.UTF_8) : null;
    }
}
