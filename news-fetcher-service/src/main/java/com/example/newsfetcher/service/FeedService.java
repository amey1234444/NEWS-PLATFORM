package com.example.newsfetcher.service;

import com.example.newsfetcher.entity.Feed;
import com.example.newsfetcher.entity.News;
import com.example.newsfetcher.producer.NewsProducer;
import com.example.newsfetcher.repository.FeedRepository;
import com.example.newsfetcher.repository.NewsRepository;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private final Logger log = LoggerFactory.getLogger(FeedService.class);
    private final NewsRepository newsRepository;
    private final NewsProducer producer;
    private final FeedRepository feedRepository;

    public FeedService(NewsRepository newsRepository, NewsProducer producer,
                       FeedRepository feedRepository) {
        this.newsRepository = newsRepository;
        this.producer = producer;
        this.feedRepository = feedRepository;
    }

    public int fetchAll() {
        int published = 0;
        List<Feed> feeds = feedRepository.findAll();
        for (Feed f : feeds) {
            if (!f.isActive()) continue;
            try {
                published += fetchSingle(f);
            } catch (Exception e) {
                log.warn("Failed to fetch feed {}: {}", f.getUrl(), e.getMessage());
            }
        }
        return published;
    }

    public int fetchSingle(Feed feed) {
        int published = 0;
        String feedUrl = feed.getUrl();
        try (InputStream in = new URL(feedUrl).openStream()) {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed synd = input.build(new XmlReader(in));
            for (SyndEntry entry : synd.getEntries()) {
                String title = entry.getTitle();
                String content = "";
                if (entry.getContents() != null && !entry.getContents().isEmpty()) {
                    content = entry.getContents().stream()
                            .map(SyndContent::getValue)
                            .collect(Collectors.joining("\n"));
                }
                if ((content == null || content.isEmpty()) && entry.getDescription() != null) {
                    content = entry.getDescription().getValue();
                }
                String link = entry.getLink();
                String hashSource = (link != null && !link.isBlank()) ? link : (title + "\n" + content);
                String hash = sha256(hashSource);

                if (newsRepository.existsByContentHash(hash)) {
                    continue;
                }

                News news = new News();
                news.setTitle(title == null ? "" : title);
                news.setContent(content == null ? "" : content);
                news.setSource(feedUrl);
                news.setContentHash(hash);
                news.setCreatedAt(LocalDateTime.now());

                News saved = newsRepository.save(news);
                producer.publish(saved);
                published++;
            }
            // on success reset failures
            feed.setFailureCount(0);
            feed.setLastSuccessAt(LocalDateTime.now());
            feed.setNextAttemptAt(LocalDateTime.now().plusSeconds(
                    feed.getPollingIntervalSeconds() != null && feed.getPollingIntervalSeconds() > 0
                            ? feed.getPollingIntervalSeconds() : 60));
            feedRepository.save(feed);
        } catch (Exception e) {
            log.warn("Error fetching {}: {}", feedUrl, e.getMessage());
            // backoff
            int failures = feed.getFailureCount() + 1;
            feed.setFailureCount(failures);
            long backoff = Math.min(3600, (long) Math.pow(2, failures)) ; // seconds, cap at 1h
            feed.setNextAttemptAt(LocalDateTime.now().plusSeconds(backoff));
            feedRepository.save(feed);
        }

        return published;
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
