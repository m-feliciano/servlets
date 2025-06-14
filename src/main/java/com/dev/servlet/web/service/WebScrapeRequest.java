package com.dev.servlet.web.service;

import com.dev.servlet.web.scrape.WebScrapeServiceEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class WebScrapeRequest {
    private final WebScrapeServiceEnum webScrapeServiceEnum;
    private final String url;
    private final Map<String, Object> params;
}
