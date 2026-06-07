package com.example.newsfetcher.repository;

import com.example.newsfetcher.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    List<Feed> findByActiveTrueAndNextAttemptAtBeforeOrderByNextAttemptAtAsc(LocalDateTime t);

    java.util.Optional<com.example.newsfetcher.entity.Feed> findByUrl(String url);

}
