package com.portfolio.urlshortener;

import com.portfolio.urlshortener.entity.Url;
import com.portfolio.urlshortener.service.UrlShortenerService;
import com.portfolio.urlshortener.util.Base62Encoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UrlShortenerApplicationTests {

    @Autowired
    private UrlShortenerService shortenerService;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        assertNotNull(shortenerService);
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
}
