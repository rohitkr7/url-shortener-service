package com.portfolio.urlshortener.service;

import com.portfolio.urlshortener.entity.Url;
import com.portfolio.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    @Test
    @DisplayName("Unit Test: shortenUrl generates correct short code from sequence and saves entity")
    void testShortenUrlSuccess() {
        String originalUrl = "https://github.com/spring-projects";
        long sequenceId = 1000000L; // Encodes to "4c92" in Base62
        Url savedUrl = new Url(originalUrl, "4c92");

        when(urlRepository.getNextSequenceId()).thenReturn(sequenceId);
        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        Url result = urlShortenerService.shortenUrl(originalUrl);

        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals("4c92", result.getShortCode());
        verify(urlRepository, times(1)).getNextSequenceId();
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    @DisplayName("Unit Test: getOriginalUrl returns destination URL when shortCode exists")
    void testGetOriginalUrlSuccess() {
        String shortCode = "4c92";
        String originalUrl = "https://github.com/spring-projects";
        Url url = new Url(originalUrl, shortCode);

        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(url));

        String result = urlShortenerService.getOriginalUrl(shortCode);

        assertEquals(originalUrl, result);
        verify(urlRepository, times(1)).findByShortCode(shortCode);
    }

    @Test
    @DisplayName("Unit Test: getOriginalUrl throws IllegalArgumentException when shortCode is not found")
    void testGetOriginalUrlNotFound() {
        String shortCode = "unknownCode";
        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            urlShortenerService.getOriginalUrl(shortCode);
        });

        assertTrue(exception.getMessage().contains("Short URL not found"));
        verify(urlRepository, times(1)).findByShortCode(shortCode);
    }
}
