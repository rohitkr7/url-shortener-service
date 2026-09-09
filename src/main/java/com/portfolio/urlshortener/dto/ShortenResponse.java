package com.portfolio.urlshortener.dto;

public record ShortenResponse(String originalUrl, String shortCode, String shortUrl) {}