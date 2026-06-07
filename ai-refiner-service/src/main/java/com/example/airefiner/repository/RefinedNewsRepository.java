package com.example.airefiner.repository;

import com.example.airefiner.entity.RefinedNews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefinedNewsRepository extends JpaRepository<RefinedNews, Long> {
}
