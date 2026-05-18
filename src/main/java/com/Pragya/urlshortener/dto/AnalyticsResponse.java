package com.Pragya.urlshortener.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AnalyticsResponse {
    private String shortCode;
    private String originalUrl;
    private Long totalClicks;
    private List<DailyClick> dailyClicks;

    @Data
    @Builder
    public static class DailyClick {
        private String date;
        private Long clicks;
    }
}
