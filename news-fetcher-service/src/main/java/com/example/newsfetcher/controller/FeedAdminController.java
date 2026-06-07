package com.example.newsfetcher.controller;

import com.example.newsfetcher.entity.Feed;
import com.example.newsfetcher.repository.FeedRepository;
import com.example.newsfetcher.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/feeds")
public class FeedAdminController {

    private final FeedRepository feedRepository;
    private final FeedService feedService;

    public FeedAdminController(FeedRepository feedRepository, FeedService feedService) {
        this.feedRepository = feedRepository;
        this.feedService = feedService;
    }

    @GetMapping
    public List<Feed> list() {
        return feedRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Feed> create(@RequestBody Feed feed) {
        if (feed.getNextAttemptAt() == null) feed.setNextAttemptAt(LocalDateTime.now());
        Feed saved = feedRepository.save(feed);
        return ResponseEntity.created(URI.create("/api/feeds/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feed> get(@PathVariable Long id) {
        return feedRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Feed> update(@PathVariable Long id, @RequestBody Feed update) {
        return feedRepository.findById(id).map(existing -> {
            existing.setUrl(update.getUrl());
            existing.setActive(update.isActive());
            existing.setPollingIntervalSeconds(update.getPollingIntervalSeconds());
            existing.setNextAttemptAt(update.getNextAttemptAt() == null ? LocalDateTime.now() : update.getNextAttemptAt());
            feedRepository.save(existing);
            return ResponseEntity.ok(existing);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feedRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<String> runNow(@PathVariable Long id) {
        return feedRepository.findById(id).map(feed -> {
            int published = feedService.fetchSingle(feed);
            return ResponseEntity.ok("published=" + published);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
