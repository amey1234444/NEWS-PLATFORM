package com.example.newsfetcher.controller;

import com.example.newsfetcher.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
public class FetchController {

    private final FeedService feedService;

    public FetchController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/fetch")
    public ResponseEntity<String> fetch() {
        int published = feedService.fetchAll();
        return ResponseEntity.ok("fetched and published count=" + published);
    }
}
