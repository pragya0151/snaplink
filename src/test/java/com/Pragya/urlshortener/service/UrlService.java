package com.Pragya.urlshortener.service;

import com.Pragya.urlshortener.dto.ShortenRequest;
import com.Pragya.urlshortener.dto.ShortenResponse;
import com.Pragya.urlshortener.model.Url;
import com.Pragya.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.short-code-length}")
    private int shortCodeLength;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    public ShortenResponse shortenUrl(ShortenRequest request) {
        String shortCode = resolveShortCode(request);

        Url url = new Url();
        url.setShortCode(shortCode);
        url.setOriginalUrl(request.getUrl());

        Url saved = urlRepository.save(url);
        return buildResponse(saved);
    }

    @Transactional
    @Cacheable(value = "urls", key = "#shortCode")
    public String resolveUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new NoSuchElementException("Short URL not found: " + shortCode));

        urlRepository.incrementClickCount(shortCode);
        return url.getOriginalUrl();
    }

    @CacheEvict(value = "urls", key = "#shortCode")
    public void evictCache(String shortCode) {
        // called when a URL is deleted or updated
    }

    public ShortenResponse getStats(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new NoSuchElementException("Short URL not found: " + shortCode));

        return buildResponse(url);
    }

    private String resolveShortCode(ShortenRequest request) {
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            if (urlRepository.existsByShortCode(request.getCustomAlias())) {
                throw new IllegalArgumentException("Custom alias already taken: " + request.getCustomAlias());
            }
            return request.getCustomAlias();
        }
        return generateUniqueCode();
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (urlRepository.existsByShortCode(code));
        return code;
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(shortCodeLength);
        for (int i = 0; i < shortCodeLength; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private ShortenResponse buildResponse(Url url) {
        return ShortenResponse.builder()
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .shortCode(url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .clickCount(url.getClickCount())
                .createdAt(url.getCreatedAt().toString())
                .build();
    }
}
