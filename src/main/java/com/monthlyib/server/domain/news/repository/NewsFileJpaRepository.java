package com.monthlyib.server.domain.news.repository;

import com.monthlyib.server.domain.news.entity.NewsFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsFileJpaRepository extends JpaRepository<NewsFile, Long> {

    List<NewsFile> findAllByNewsId(Long newsId);

    void deleteAllByNewsId(Long newsId);
}
