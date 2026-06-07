package com.example.newsfetcher.scheduler;

import com.example.newsfetcher.entity.Feed;
import com.example.newsfetcher.lock.RedisLockService;
import com.example.newsfetcher.repository.FeedRepository;
import com.example.newsfetcher.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class FeedScheduler {

    private final Logger log = LoggerFactory.getLogger(FeedScheduler.class);
    private final FeedRepository feedRepository;
    private final FeedService feedService;
    private final RedisLockService lockService;

    @Value("${fetcher.maxConcurrent:4}")
    private int maxConcurrent;

    @Value("${fetcher.defaultPollingIntervalSeconds:60}")
    private int defaultPollingIntervalSeconds;

    public FeedScheduler(FeedRepository feedRepository, FeedService feedService, RedisLockService lockService) {
        this.feedRepository = feedRepository;
        this.feedService = feedService;
        this.lockService = lockService;
    }

    @Scheduled(fixedDelayString = "${fetcher.pollIntervalMs:15000}")
    public void pollDueFeeds() {
        LocalDateTime now = LocalDateTime.now();
        List<Feed> due = feedRepository.findByActiveTrueAndNextAttemptAtBeforeOrderByNextAttemptAtAsc(now);
        if (due.isEmpty()) return;
        log.info("Found {} due feeds", due.size());
        int count = 0;
        for (Feed f : due) {
            if (count >= maxConcurrent) break;
            try {
                String lockKey = "feed-lock:" + f.getId();
                int lease = (f.getPollingIntervalSeconds() != null && f.getPollingIntervalSeconds() > 0)
                        ? f.getPollingIntervalSeconds() : defaultPollingIntervalSeconds;
                boolean acquired = lockService.tryLock(lockKey, 0, Math.max(lease, 60), TimeUnit.SECONDS);
                if (!acquired) continue;
                try {
                    feedService.fetchSingle(f);
                    count++;
                } finally {
                    lockService.unlock(lockKey);
                }
            } catch (Exception e) {
                log.warn("Failed to process feed {}: {}", f.getUrl(), e.getMessage());
            }
        }
    }
}
