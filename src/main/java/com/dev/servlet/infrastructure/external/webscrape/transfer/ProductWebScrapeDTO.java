package com.dev.servlet.infrastructure.external.webscrape.transfer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductWebScrapeDTO {
    private String title;
    private Double price;
    private String thumbnail;
    private String category;
    private String description;
    @JsonFormat(pattern = "yyyy-M-d")
    @JsonProperty("publish_date")
    private Date publishDate;
}
