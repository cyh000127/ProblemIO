package com.problemio.question.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionResponse {
    private Long id;
    private int order;
    private String description;
    private String imageUrl;
    private List<QuestionAnswerDto> answers;
}
