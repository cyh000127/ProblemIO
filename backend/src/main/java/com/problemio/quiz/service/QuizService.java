package com.problemio.quiz.service;

import com.problemio.quiz.dto.*;

import java.util.Map;
import java.util.List;

public interface QuizService {

    Map<String, Object> getQuizzes(int page, int size, String sort, String keyword);

    QuizResponse createQuiz(Long userId, QuizCreateRequest request);

    QuizResponse updateQuiz(Long userId, Long quizId, QuizUpdateRequest request);

    void deleteQuiz(Long userId, Long quizId);

    QuizResponse getQuiz(Long quizId, Long viewerId);

    List<QuizSummaryDto> getPublicQuizzes();

    List<QuizSummaryDto> getUserQuizzes(Long userId);

    void likeQuiz(Long userId, Long quizId);

    void unlikeQuiz(Long userId, Long quizId);

    List<QuizSummaryDto> getQuizzesOfFollowings(Long userId, int page, int size);
    List<QuizSummaryDto> getLikedQuizzes(Long userId, int page, int size);
}
