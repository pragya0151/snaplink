package com.Pragya.urlshortener.service;

import com.Pragya.urlshortener.dto.AnalyticsResponse;
import com.Pragya.urlshortener.model.Url;
import com.Pragya.urlshortener.model.UrlClick;
import com.Pragya.urlshortener.repository.UrlClickRepository;
import com.Pragya.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UrlClickRepository urlClickRepository;
    private final UrlRepository urlRepository;

    @Transactional
    public void recordClick(String shortCode) {
        LocalDate today = LocalDate.now();
        int updated = urlClickRepository.incrementClickCount(shortCode, today);
        if (updated == 0) {
            UrlClick click = new UrlClick();
            click.setShortCode(shortCode);
            click.setClickDate(today);
            click.setClickCount(1L);
            urlClickRepository.save(click);
        }
    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new NoSuchElementException("Short URL not found: " + shortCode));

        List<UrlClick> clicks = urlClickRepository
                .findByShortCodeOrderByClickDateDesc(shortCode);

        List<AnalyticsResponse.DailyClick> dailyClicks = clicks.stream()
                .map(c -> AnalyticsResponse.DailyClick.builder()
                        .date(c.getClickDate().toString())
                        .clicks(c.getClickCount())
                        .build())
                .toList();

        return AnalyticsResponse.builder()
                .shortCode(shortCode)
                .originalUrl(url.getOriginalUrl())
                .totalClicks(url.getClickCount())
                .dailyClicks(dailyClicks)
                .build();
    }
}