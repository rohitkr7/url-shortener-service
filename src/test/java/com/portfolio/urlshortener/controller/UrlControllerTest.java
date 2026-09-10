package com.portfolio.urlshortener.controller;

import com.portfolio.urlshortener.entity.Url;
import com.portfolio.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UrlControllerTest {

    @Mock
    private UrlShortenerService shortenerService;

    @InjectMocks
    private UrlController urlController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(urlController).build();
    }

    @Test
    @DisplayName("Unit Test: POST /api/v1/urls creates short URL and returns 201 Created")
    void testCreateShortUrl() throws Exception {
        String originalUrl = "https://spring.io";
        String shortCode = "4c92";
        Url url = new Url(originalUrl, shortCode);

        when(shortenerService.shortenUrl(originalUrl)).thenReturn(url);

        String jsonPayload = """
                {
                    "url": "https://spring.io"
                }
                """;

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl", is(originalUrl)))
                .andExpect(jsonPath("$.shortCode", is(shortCode)))
                .andExpect(jsonPath("$.shortUrl", is("http://localhost:9090/" + shortCode)));

        verify(shortenerService, times(1)).shortenUrl(originalUrl);
    }

    @Test
    @DisplayName("Unit Test: GET /{shortCode} returns 302 Found redirect to target URL")
    void testRedirect() throws Exception {
        String shortCode = "4c92";
        String destinationUrl = "https://spring.io";

        when(shortenerService.getOriginalUrl(shortCode)).thenReturn(destinationUrl);

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", destinationUrl));

        verify(shortenerService, times(1)).getOriginalUrl(shortCode);
    }
}
