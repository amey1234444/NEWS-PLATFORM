package com.example.newsfetcher.repository;

import com.example.newsfetcher.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
	boolean existsByContentHash(String contentHash);
}
