package com.dev.servlet.web.scrape.impl;

import com.dev.servlet.web.scrape.IWebScrapeService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public abstract class ScrapeService<T> implements IWebScrapeService<T> {
    protected OkHttpClient client;
    protected ObjectMapper objectMapper;

    public ScrapeService() {
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
