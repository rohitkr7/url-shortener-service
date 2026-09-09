package com.portfolio.urlshortener.service;

import com.portfolio.urlshortener.entity.Url;
import com.portfolio.urlshortener.repository.UrlRepository;
import com.portfolio.urlshortener.util.Base62Encoder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlShortenerService {

    private final UrlRepository urlRepository;

    public UrlShortenerService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Transactional
    public Url shortenUrl(String originalUrl) {
        Long nextId = urlRepository.getNextSequenceId();
        String shortCode = Base62Encoder.encode(nextId);

        Url url = new Url(originalUrl, shortCode);
        return urlRepository.save(url);
    }

    @Cacheable(value = "urls", key = "#shortCode")
    public String getOriginalUrl(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .map(Url::getOriginalUrl)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found for code: " + shortCode));
    }
}