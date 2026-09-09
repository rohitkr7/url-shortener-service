package com.portfolio.urlshortener.repository;

import com.portfolio.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    // Fetch next value directly from sequence so Base62 can encode before saving
    @Query(value = "SELECT nextval('url_id_seq')", nativeQuery = true)
    Long getNextSequenceId();
}