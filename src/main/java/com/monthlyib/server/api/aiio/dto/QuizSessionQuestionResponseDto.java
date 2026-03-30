package com.monthlyib.server.api.aiio.dto;

import com.monthlyib.server.domain.aiio.entity.QuizSessionQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSessionQuestionResponseDto {

    private Long questionId;
    private Long chapterTestId;
    private String question;
    private String choiceA;
    private String choiceB;
    private String choiceC;
    private String choiceD;
    private String imagePath;
    private String userAnswer;
    private Boolean isCorrect;
    private Integer elapsedTime;

    /**
     * Student view: does NOT include the correct answer.
     */
    public static QuizSessionQuestionResponseDto of(QuizSessionQuestion q) {
        QuizSessionQuestionResponseDto dto = new QuizSessionQuestionResponseDto();
        dto.setQuestionId(q.getId());
        dto.setChapterTestId(q.getChapterTest().getId());
        dto.setQuestion(q.getChapterTest().getQuestion());
        dto.setChoiceA(q.getChapterTest().getChoiceA());
        dto.setChoiceB(q.getChapterTest().getChoiceB());
        dto.setChoiceC(q.getChapterTest().getChoiceC());
        dto.setChoiceD(q.getChapterTest().getChoiceD());
        dto.setImagePath(q.getChapterTest().getImagePath());
        dto.setUserAnswer(q.getUserAnswer());
        dto.setIsCorrect(q.isCorrect());
        dto.setElapsedTime(q.getElapsedTime());
        return dto;
    }
}
