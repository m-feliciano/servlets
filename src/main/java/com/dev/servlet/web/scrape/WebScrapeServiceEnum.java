package com.dev.servlet.web.scrape;

import com.dev.servlet.web.scrape.impl.ProductWebScrapeService;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebScrapeServiceEnum {
    PRODUCT("Product", new ProductWebScrapeService()),
    ;

    private final String name;
    private final IWebScrapeService<?> webScrapeService;
}
