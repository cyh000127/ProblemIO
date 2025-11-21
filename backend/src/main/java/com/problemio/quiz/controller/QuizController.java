package com.problemio.quiz.controller;

import com.problemio.global.auth.CustomUserDetails;
import com.problemio.global.common.ApiResponse;
import com.problemio.quiz.dto.QuizCreateRequest;
import com.problemio.quiz.dto.QuizResponse;
import com.problemio.quiz.dto.QuizSummaryDto;
import com.problemio.quiz.dto.QuizUpdateRequest;
import com.problemio.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQuizzes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizzes(page, size, sort, keyword)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @RequestBody @Valid QuizCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(ApiResponse.success(quizService.createQuiz(userId, request)));
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody @Valid QuizUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(ApiResponse.success(quizService.updateQuiz(userId, quizId, request)));
    }

    @PatchMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuizPatch(
            @PathVariable Long quizId,
            @RequestBody QuizUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(ApiResponse.success(quizService.updateQuiz(userId, quizId, request)));
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long quizId,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        quizService.deleteQuiz(userId, quizId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long viewerId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuiz(quizId, viewerId)));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<QuizSummaryDto>>> getPublicQuizzes() {
        return ResponseEntity.ok(ApiResponse.success(quizService.getPublicQuizzes()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<QuizSummaryDto>>> getMyQuizzes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getUserQuizzes(userDetails.getUser().getId())));
    }

    @PostMapping("/{quizId}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> likeQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        quizService.likeQuiz(userDetails.getUser().getId(), quizId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("liked", true)));
    }

    @DeleteMapping("/{quizId}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unlikeQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        quizService.unlikeQuiz(userDetails.getUser().getId(), quizId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("liked", false)));
    }
}
