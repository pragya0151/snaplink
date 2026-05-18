package com.Pragya.urlshortener;

import com.Pragya.urlshortener.repository.UrlClickRepository;
import com.Pragya.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.cache.type=simple",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
class UrlControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private UrlClickRepository urlClickRepository;

    @BeforeEach
    void setUp() {
        urlClickRepository.deleteAll();
        urlRepository.deleteAll();
    }

    @Test
    void shortenUrl_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://www.google.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(jsonPath("$.originalUrl").value("https://www.google.com"))
                .andExpect(jsonPath("$.clickCount").value(0));
    }

    @Test
    void shortenUrl_invalidUrl_returns400() throws Exception {
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"not-a-valid-url\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shortenUrl_customAlias_usesAlias() throws Exception {
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://github.com\", \"customAlias\": \"gh-test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("gh-test"));
    }

    @Test
    void shortenUrl_duplicateAlias_returns400() throws Exception {
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://github.com\", \"customAlias\": \"dup\"}"));

        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://github.com\", \"customAlias\": \"dup\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redirect_validCode_returns301() throws Exception {
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://www.google.com\", \"customAlias\": \"redir-test\"}"));

        mockMvc.perform(get("/redir-test"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "https://www.google.com"));
    }

    @Test
    void redirect_invalidCode_returns404() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStats_validCode_returnsStats() throws Exception {
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://www.google.com\", \"customAlias\": \"stats-test\"}"));

        mockMvc.perform(get("/api/stats/stats-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("stats-test"))
                .andExpect(jsonPath("$.clickCount").value(0));
    }

    @Test
    void getAnalytics_validCode_returnsAnalytics() throws Exception {
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://www.google.com\", \"customAlias\": \"ana-test\"}"));

        mockMvc.perform(get("/api/analytics/ana-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("ana-test"))
                .andExpect(jsonPath("$.totalClicks").value(0));
    }

    @Test
    void shortenUrl_withExpiry_setsExpiryDate() throws Exception {
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://www.google.com\", \"expiryDays\": 7}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists());
    }
}