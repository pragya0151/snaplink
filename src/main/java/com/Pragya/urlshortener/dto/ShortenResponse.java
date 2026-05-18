package com.Pragya.urlshortener.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortenResponse {
    private String shortUrl;
    private String shortCode;
    private String originalUrl;
    private Long clickCount;
    private String createdAt;
    private String expiresAt;
}
