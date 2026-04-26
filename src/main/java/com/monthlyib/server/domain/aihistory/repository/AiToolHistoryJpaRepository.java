package com.monthlyib.server.domain.aihistory.repository;

import com.monthlyib.server.domain.aihistory.entity.AiToolHistory;
import com.monthlyib.server.domain.aihistory.entity.AiToolType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiToolHistoryJpaRepository extends JpaRepository<AiToolHistory, Long> {

    Page<AiToolHistory> findByUserUserId(Long userId, Pageable pageable);

    Page<AiToolHistory> findByUserUserIdAndToolType(Long userId, AiToolType toolType, Pageable pageable);

    Optional<AiToolHistory> findByAiToolHistoryIdAndUserUserId(Long aiToolHistoryId, Long userId);

    List<AiToolHistory> findAllByUserUserId(Long userId);
}
