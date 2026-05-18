package com.Pragya.urlshortener.repository;

import com.Pragya.urlshortener.model.UrlClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UrlClickRepository extends JpaRepository<UrlClick, Long> {

    List<UrlClick> findByShortCodeOrderByClickDateDesc(String shortCode);

    @Modifying
    @Query("""
        UPDATE UrlClick u SET u.clickCount = u.clickCount + 1
        WHERE u.shortCode = :shortCode AND u.clickDate = :clickDate
        """)
    int incrementClickCount(String shortCode, java.time.LocalDate clickDate);
}