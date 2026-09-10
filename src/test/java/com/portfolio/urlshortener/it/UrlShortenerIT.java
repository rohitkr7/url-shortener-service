package com.portfolio.urlshortener.it;

import com.portfolio.urlshortener.entity.Url;
import com.portfolio.urlshortener.repository.UrlRepository;
import com.portfolio.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class UrlShortenerIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UrlShortenerService shortenerService;

    @Autowired
    private UrlRepository urlRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Integration Test: Application context loads and initializes all beans successfully")
    void contextLoads() {
        assertNotNull(shortenerService);
        assertNotNull(urlRepository);
        assertNotNull(mockMvc);
    }

    @Test
    @DisplayName("Integration Test: End-to-end URL shortening and retrieval persistence flow")
    void testEndToEndShortenAndRetrieveFlow() {
        String originalUrl = "https://spring.io/projects/spring-boot";
        Url shortened = shortenerService.shortenUrl(originalUrl);

        assertNotNull(shortened);
        assertNotNull(shortened.getId());
        assertNotNull(shortened.getShortCode());
        assertEquals(originalUrl, shortened.getOriginalUrl());

        // Verify direct repository lookup in PostgreSQL
        var foundOptional = urlRepository.findByShortCode(shortened.getShortCode());
        assertTrue(foundOptional.isPresent());
        assertEquals(originalUrl, foundOptional.get().getOriginalUrl());

        // Verify service retrieval & cache lookup
        String retrievedUrl = shortenerService.getOriginalUrl(shortened.getShortCode());
        assertEquals(originalUrl, retrievedUrl);
    }

    @Test
    @DisplayName("Integration Test: REST API POST /api/v1/urls followed by GET /{shortCode} redirection")
    void testEndToEndRestApiFlow() throws Exception {
        String targetUrl = "https://developer.mozilla.org";
        String requestJson = """
                {
                    "url": "%s"
                }
                """.formatted(targetUrl);

        // 1. Create short URL via API
        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl", is(targetUrl)))
                .andExpect(jsonPath("$.shortCode", notNullValue()))
                .andExpect(jsonPath("$.shortUrl", startsWith("http://localhost:9090/")));

        // Extract generated shortCode from service
        Url savedUrl = shortenerService.shortenUrl(targetUrl);

        // 2. Perform redirect via API
        mockMvc.perform(get("/" + savedUrl.getShortCode()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", targetUrl));
    }
}
