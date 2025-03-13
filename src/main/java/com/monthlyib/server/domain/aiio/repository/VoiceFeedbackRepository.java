package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface VoiceFeedbackRepository {

    Optional<VoiceFeedback> findFeedback(Long feedbackId);

    Page<VoiceFeedback> findAllFeedback(Pageable pageable);

    VoiceFeedback saveFeedback(VoiceFeedback voiceFeedback);

    void deleteFeedback(Long feedbackId);

    Optional<VoiceFeedback> findFeedbackByAuthorId(Long authorId);
}