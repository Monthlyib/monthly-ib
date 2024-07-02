package com.monthlyib.server.domain.news.repository;

import com.monthlyib.server.domain.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsJpaRepository extends JpaRepository<News, Long> {
}
