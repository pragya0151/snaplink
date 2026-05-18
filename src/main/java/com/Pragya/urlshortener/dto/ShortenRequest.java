package com.Pragya.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class ShortenRequest {

    @NotBlank(message = "URL cannot be blank")
    @Pattern(
        regexp = "^(https?://).*",
        message = "URL must start with http:// or https://"
    )
    private String url;

    private String customAlias;

    @Min(value = 1, message = "Expiry days must be at least 1")
    @Max(value = 365, message = "Expiry days cannot exceed 365")
    private Integer expiryDays;
}