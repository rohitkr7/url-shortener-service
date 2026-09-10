package com.portfolio.urlshortener;

import com.portfolio.urlshortener.entity.Url;
import com.portfolio.urlshortener.repository.UrlRepository;
import com.portfolio.urlshortener.service.UrlShortenerService;
import com.portfolio.urlshortener.util.Base62Encoder;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class UrlShortenerApplicationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private UrlShortenerService shortenerService;

    @Autowired
    private UrlRepository urlRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        assertNotNull(shortenerService);
        assertNotNull(mockMvc);
        assertNotNull(urlRepository);
    }

    @Test
    @DisplayName("Base62 Encoder correctly encodes positive, zero, and boundary values")
    void testBase62Encoder() {
        assertEquals("0", Base62Encoder.encode(0));
        assertEquals("0", Base62Encoder.encode(-10));
        assertEquals("1", Base62Encoder.encode(1));
        assertEquals("Z", Base62Encoder.encode(61));
        assertEquals("10", Base62Encoder.encode(62));
        assertEquals("4c92", Base62Encoder.encode(1000000));
    }

    @Test
    @DisplayName("Should successfully shorten a URL and retrieve original URL")
    void testShortenAndRetrieveUrl() {
        String originalUrl = "https://spring.io/projects/spring-boot";
        Url shortened = shortenerService.shortenUrl(originalUrl);

        assertNotNull(shortened);
        assertNotNull(shortened.getId());
        assertNotNull(shortened.getShortCode());
        assertEquals(originalUrl, shortened.getOriginalUrl());

        String retrievedUrl = shortenerService.getOriginalUrl(shortened.getShortCode());
        assertEquals(originalUrl, retrievedUrl);
    }

    @Test
    @DisplayName("Should throw exception when retrieving non-existent short code")
    void testGetNonExistentUrlThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            shortenerService.getOriginalUrl("nonExistentCode999");
        });
    }

    @Test
    @DisplayName("Repository should find entity by short code")
    void testRepositoryFindByShortCode() {
        Url url = shortenerService.shortenUrl("https://github.com/spring-projects");
        var foundOptional = urlRepository.findByShortCode(url.getShortCode());

        assertTrue(foundOptional.isPresent());
        assertEquals(url.getOriginalUrl(), foundOptional.get().getOriginalUrl());
    }

    @Test
    @DisplayName("POST /api/v1/urls should create short URL and return 201 Created")
    void testCreateShortUrlApi() throws Exception {
        String requestJson = """
                {
                    "url": "https://docs.spring.io"
                }
                """;

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl", is("https://docs.spring.io")))
                .andExpect(jsonPath("$.shortCode", notNullValue()))
                .andExpect(jsonPath("$.shortUrl", startsWith("http://localhost:9090/")));
    }

    @Test
    @DisplayName("GET /{shortCode} should return 302 Found redirect with Location header")
    void testRedirectApi() throws Exception {
        String targetUrl = "https://developer.mozilla.org";
        Url shortened = shortenerService.shortenUrl(targetUrl);

        mockMvc.perform(get("/" + shortened.getShortCode()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", targetUrl));
    }
}
