package com.dev.servlet.infrastructure.external.webscrape.api;
import com.dev.servlet.infrastructure.external.webscrape.IWebScrapeService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

public abstract class ScrapeApiClient<TResponse> implements IWebScrapeService<TResponse> {
    protected OkHttpClient client;
    protected ObjectMapper objectMapper;
    public ScrapeApiClient() {
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
