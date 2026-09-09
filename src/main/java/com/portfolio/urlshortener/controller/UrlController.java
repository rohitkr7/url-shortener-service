package com.portfolio.urlshortener.controller;

import com.portfolio.urlshortener.dto.ShortenRequest;
import com.portfolio.urlshortener.dto.ShortenResponse;
import com.portfolio.urlshortener.entity.Url;
import com.portfolio.urlshortener.service.UrlShortenerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlController {

    private final UrlShortenerService shortenerService;

    public UrlController(UrlShortenerService shortenerService) {
        this.shortenerService = shortenerService;
    }

    @PostMapping("/api/v1/urls")
    public ResponseEntity<ShortenResponse> createShortUrl(@RequestBody ShortenRequest request) {
        Url url = shortenerService.shortenUrl(request.url());
        String shortUrl = "http://localhost:9090/" + url.getShortCode();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ShortenResponse(url.getOriginalUrl(), url.getShortCode(), shortUrl));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = shortenerService.getOriginalUrl(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND) // 302 redirect
                .location(URI.create(originalUrl))
                .build();
    }
}