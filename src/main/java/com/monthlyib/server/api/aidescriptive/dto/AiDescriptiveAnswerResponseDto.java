package com.monthlyib.server.api.aidescriptive.dto;

import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveAnswer;
import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveTest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDescriptiveAnswerResponseDto {

    private Long id;
    private Long questionId;
    private String subject;
    private String chapter;
    private String question;
    private Integer maxScore;
    private String answerText;
    private String feedbackEnglish;
    private String feedbackKorean;
    private Integer score;
    private String modelAnswer;
    private String imagePath;

    public static AiDescriptiveAnswerResponseDto of(AiDescriptiveAnswer answer, AiDescriptiveTest test) {
        AiDescriptiveAnswerResponseDto dto = new AiDescriptiveAnswerResponseDto();
        dto.setId(answer.getId());
        dto.setQuestionId(test.getId());
        dto.setSubject(test.getSubject());
        dto.setChapter(test.getChapter());
        dto.setQuestion(test.getQuestion());
        dto.setMaxScore(answer.getMaxScore());
        dto.setAnswerText(answer.getAnswerText());
        dto.setFeedbackEnglish(answer.getFeedbackEnglish());
        dto.setFeedbackKorean(answer.getFeedbackKorean());
        dto.setScore(answer.getScore());
        dto.setModelAnswer(answer.getModelAnswer());
        dto.setImagePath(test.getImagePath());
        return dto;
    }
}
