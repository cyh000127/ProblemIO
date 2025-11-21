package com.problemio.quiz.service;

import com.problemio.follow.mapper.FollowMapper;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.question.domain.Question;
import com.problemio.question.domain.QuestionAnswer;
import com.problemio.question.dto.AnswerCreateRequest;
import com.problemio.question.dto.QuestionAnswerDto;
import com.problemio.question.dto.QuestionCreateRequest;
import com.problemio.question.dto.QuestionResponse;
import com.problemio.question.mapper.QuestionAnswerMapper;
import com.problemio.question.mapper.QuestionMapper;
import com.problemio.quiz.domain.Quiz;
import com.problemio.quiz.dto.QuizCreateRequest;
import com.problemio.quiz.dto.QuizResponse;
import com.problemio.quiz.dto.QuizSummaryDto;
import com.problemio.quiz.dto.QuizUpdateRequest;
import com.problemio.quiz.mapper.QuizLikeMapper;
import com.problemio.quiz.mapper.QuizMapper;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuizMapper quizMapper;
    private final QuizLikeMapper quizLikeMapper;
    private final QuestionMapper questionMapper;
    private final QuestionAnswerMapper questionAnswerMapper;
    private final UserMapper userMapper;
    private final FollowMapper followMapper;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getQuizzes(int page, int size, String sort, String keyword) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<Quiz> quizzes = quizMapper.searchQuizzes(offset, safeSize, sort, keyword);
        int total = quizMapper.countQuizzes(keyword);

        List<QuizSummaryDto> content = quizzes.stream()
                .map(q -> QuizSummaryDto.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .thumbnailUrl(q.getThumbnailUrl())
                        .likeCount(q.getLikeCount())
                        .playCount(q.getPlayCount())
                        .build())
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) total / safeSize);

        return Map.of(
                "content", content,
                "totalPages", totalPages,
                "totalElements", total
        );
    }

    @Override
    public QuizResponse createQuiz(Long userId, QuizCreateRequest request) {
        Quiz quiz = new Quiz();
        quiz.setUserId(userId);
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setThumbnailUrl(request.getThumbnailUrl());
        quiz.setPublic(request.isPublic());
        quiz.setLikeCount(0);
        quiz.setPlayCount(0);

        quizMapper.insertQuiz(quiz);
        saveQuestions(quiz.getId(), request.getQuestions());

        return buildQuizResponse(quiz, null, null, null);
    }

    @Override
    public QuizResponse updateQuiz(Long userId, Long quizId, QuizUpdateRequest request) {
        Quiz quiz = quizMapper.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        if (!Objects.equals(quiz.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (request.getTitle() != null) {
            quiz.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            quiz.setDescription(request.getDescription());
        }
        if (request.getThumbnailUrl() != null) {
            quiz.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getIsPublic() != null) {
            quiz.setPublic(request.getIsPublic());
        }

        quizMapper.updateQuiz(quiz);
        if (request.getQuestions() != null) {
            List<Question> existing = questionMapper.findByQuizId(quizId);
            for (Question question : existing) {
                questionAnswerMapper.deleteByQuestionId(question.getId());
            }
            questionMapper.deleteByQuizId(quizId);
            saveQuestions(quizId, request.getQuestions());
        }
        return buildQuizResponse(quiz, loadQuestions(quizId), findAuthor(quiz.getUserId()), null);
    }

    @Override
    public void deleteQuiz(Long userId, Long quizId) {
        Quiz quiz = quizMapper.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        if (!Objects.equals(quiz.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 하위 질문/정답 삭제
        List<Question> questions = questionMapper.findByQuizId(quizId);
        for (Question question : questions) {
            questionAnswerMapper.deleteByQuestionId(question.getId());
        }
        questionMapper.deleteByQuizId(quizId);

        quizMapper.deleteQuiz(quizId);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResponse getQuiz(Long quizId, Long viewerId) {
        Quiz quiz = quizMapper.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        UserResponse author = findAuthor(quiz.getUserId());
        List<QuestionResponse> questions = loadQuestions(quizId);

        Boolean isLikedByMe = null;
        Boolean isFollowedByMe = null;
        if (viewerId != null) {
            isLikedByMe = quizLikeMapper.findByUserIdAndQuizId(viewerId, quizId).isPresent();
            isFollowedByMe = followMapper.exists(viewerId, quiz.getUserId()) > 0;
        }

        return buildQuizResponse(quiz, questions, author, isLikedByMe, isFollowedByMe);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDto> getPublicQuizzes() {
        return quizMapper.findPublicQuizzes().stream()
                .map(q -> QuizSummaryDto.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .thumbnailUrl(q.getThumbnailUrl())
                        .likeCount(q.getLikeCount())
                        .playCount(q.getPlayCount())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDto> getUserQuizzes(Long userId) {
        return quizMapper.findQuizzesByUserId(userId).stream()
                .map(q -> QuizSummaryDto.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .thumbnailUrl(q.getThumbnailUrl())
                        .likeCount(q.getLikeCount())
                        .playCount(q.getPlayCount())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void likeQuiz(Long userId, Long quizId) {
        quizMapper.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        boolean alreadyLiked = quizLikeMapper.findByUserIdAndQuizId(userId, quizId).isPresent();
        if (alreadyLiked) {
            return;
        }
        quizLikeMapper.insertQuizLike(buildQuizLike(userId, quizId));
        quizMapper.incrementLikeCount(quizId);
    }

    @Override
    public void unlikeQuiz(Long userId, Long quizId) {
        quizMapper.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        quizLikeMapper.deleteQuizLike(userId, quizId);
        quizMapper.decrementLikeCount(quizId);
    }

    private QuizResponse buildQuizResponse(Quiz quiz,
                                           List<QuestionResponse> questions,
                                           UserResponse author,
                                           Boolean isLikedByMe) {
        return buildQuizResponse(quiz, questions, author, isLikedByMe, null);
    }

    private QuizResponse buildQuizResponse(Quiz quiz,
                                           List<QuestionResponse> questions,
                                           UserResponse author,
                                           Boolean isLikedByMe,
                                           Boolean isFollowedByMe) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .userId(quiz.getUserId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .thumbnailUrl(quiz.getThumbnailUrl())
                .isPublic(quiz.isPublic())
                .likeCount(quiz.getLikeCount())
                .playCount(quiz.getPlayCount())
                .questions(questions)
                .author(author)
                .isLikedByMe(isLikedByMe)
                .isFollowedByMe(isFollowedByMe)
                .build();
    }

    private void saveQuestions(Long quizId, List<QuestionCreateRequest> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }

        int index = 1;
        for (QuestionCreateRequest request : questions) {
            Question question = new Question();
            question.setQuizId(quizId);
            int order = request.getQuestionOrder() != null ? request.getQuestionOrder() : index;
            question.setQuestionOrder(order);
            question.setImageUrl(request.getImageUrl());
            question.setDescription(request.getDescription());
            questionMapper.insertQuestion(question);

            saveAnswers(question.getId(), request.getAnswers());
            index++;
        }
    }

    private void saveAnswers(Long questionId, List<AnswerCreateRequest> answers) {
        if (answers == null || answers.isEmpty()) {
            return;
        }
        int idx = 1;
        for (AnswerCreateRequest answerRequest : answers) {
            if (answerRequest.getAnswerText() == null || answerRequest.getAnswerText().isBlank()) {
                continue;
            }
            QuestionAnswer answer = new QuestionAnswer();
            answer.setQuestionId(questionId);
            answer.setAnswerText(answerRequest.getAnswerText());
            answer.setSortOrder(answerRequest.getSortOrder() != null ? answerRequest.getSortOrder() : idx);
            questionAnswerMapper.insertAnswer(answer);
            idx++;
        }
    }

    private List<QuestionResponse> loadQuestions(Long quizId) {
        List<Question> questions = questionMapper.findByQuizId(quizId);
        return questions.stream()
                .map(q -> QuestionResponse.builder()
                        .id(q.getId())
                        .order(q.getQuestionOrder())
                        .description(q.getDescription())
                        .imageUrl(q.getImageUrl())
                        .answers(loadAnswers(q.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<QuestionAnswerDto> loadAnswers(Long questionId) {
        List<QuestionAnswer> answers = questionAnswerMapper.findByQuestionId(questionId);
        return answers.stream()
                .map(a -> {
                    QuestionAnswerDto dto = new QuestionAnswerDto();
                    dto.setId(a.getId());
                    dto.setAnswerText(a.getAnswerText());
                    dto.setSortOrder(a.getSortOrder());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private UserResponse findAuthor(Long userId) {
        return userMapper.findById(userId).orElse(null);
    }

    private com.problemio.quiz.domain.QuizLike buildQuizLike(Long userId, Long quizId) {
        com.problemio.quiz.domain.QuizLike like = new com.problemio.quiz.domain.QuizLike();
        like.setUserId(userId);
        like.setQuizId(quizId);
        return like;
    }
}
