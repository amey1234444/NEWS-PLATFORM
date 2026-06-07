package com.example.newsfetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NewsFetcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewsFetcherApplication.class, args);
    }
}
