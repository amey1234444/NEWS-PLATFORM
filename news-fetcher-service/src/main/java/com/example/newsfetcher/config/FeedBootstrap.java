package com.example.newsfetcher.config;

import com.example.newsfetcher.entity.Feed;
import com.example.newsfetcher.repository.FeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class FeedBootstrap implements ApplicationRunner {

    private final Logger log = LoggerFactory.getLogger(FeedBootstrap.class);
    private final FeedRepository feedRepository;

    @Value("${feeds.urls:}")
    private String feedsCsv;

    @Value("${fetcher.defaultPollingIntervalSeconds:60}")
    private int defaultPolling;

    public FeedBootstrap(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (feedsCsv == null || feedsCsv.isBlank()) return;
        Arrays.stream(feedsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(url -> {
                    feedRepository.findByUrl(url).ifPresentOrElse(f -> {
                        // already present
                    }, () -> {
                        Feed feed = new Feed();
                        feed.setUrl(url);
                        feed.setActive(true);
                        feed.setPollingIntervalSeconds(defaultPolling);
                        feed.setNextAttemptAt(LocalDateTime.now());
                        feedRepository.save(feed);
                        log.info("Bootstrapped feed {}", url);
                    });
                });
    }
}
