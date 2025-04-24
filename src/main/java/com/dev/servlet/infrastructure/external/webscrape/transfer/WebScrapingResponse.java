package com.dev.servlet.infrastructure.external.webscrape.transfer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.List;

@Getter
public class WebScrapingResponse<T> {
    @JsonProperty("next_url")
    private String nextUrl;
    @JsonProperty("page_number")
    private int pageNumber;
    @JsonProperty("page_size")
    private int pageSize;
    @JsonProperty("page_total")
    private int pageTotal;
    @JsonProperty("total_results")
    private int totalResults;
    @JsonProperty("results")
    private List<T> content;
}
