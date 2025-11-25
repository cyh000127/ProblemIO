package com.problemio.submission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmissionRequest {

    /**
     * 기존 진행 중인 제출 식별자. 없으면 새로 생성된다.
     */
    private Long submissionId;

    /**
     * 답안을 제출할 문제 ID
     */
    @NotNull
    private Long questionId;

    /**
     * 사용자가 입력한 단답형 답안
     */
    @NotNull
    private String answerText;
}
