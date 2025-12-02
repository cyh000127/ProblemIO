package com.problemio.comment.service;

import com.problemio.comment.domain.Comment;
import com.problemio.comment.domain.CommentLike;
import com.problemio.comment.dto.CommentCreateRequest;
import com.problemio.comment.dto.CommentResponse;
import com.problemio.comment.dto.CommentUpdateRequest;
import com.problemio.comment.mapper.CommentLikeMapper;
import com.problemio.comment.mapper.CommentMapper;
import com.problemio.quiz.mapper.QuizMapper;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final UserMapper userMapper;
    private final QuizMapper quizMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void createComment(Long quizId, Long userId, CommentCreateRequest request, String writerIp) {
        if (quizMapper.findById(quizId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 퀴즈입니다.");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력하세요.");
        }

        Comment comment = new Comment();
        comment.setQuizId(quizId);
        comment.setUserId(userId);

        if (userId == null) {
            if (request.getNickname() == null || request.getNickname().isBlank()) {
                throw new IllegalArgumentException("닉네임을 입력하세요.");
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new IllegalArgumentException("비밀번호를 입력하세요.");
            }

            comment.setGuestNickname(request.getNickname());
            comment.setGuestPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setWriterIp(writerIp);
        comment.setDeleted(false);

        commentMapper.insertComment(comment);
    }

    @Override
    @Transactional
    public void updateComment(Long commentId, Long userId, CommentUpdateRequest request) {
        Comment existing = commentMapper.findById(commentId);
        if (existing == null || existing.isDeleted()) {
            throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
        }

        if (existing.getUserId() != null) {
            if (userId == null || !existing.getUserId().equals(userId)) {
                throw new IllegalStateException("본인 댓글만 수정할 수 있습니다.");
            }
        } else {
            // 게스트 댓글
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new IllegalArgumentException("비밀번호를 입력하세요.");
            }
            if (!passwordEncoder.matches(request.getPassword(), existing.getGuestPasswordHash())) {
                throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
            }
        }

        existing.setContent(request.getContent());
        commentMapper.updateComment(existing);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId, String guestPassword) {
        Comment existing = commentMapper.findById(commentId);
        if (existing == null || existing.isDeleted()) {
            throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
        }

        if (existing.getUserId() != null) {
            if (userId == null || !existing.getUserId().equals(userId)) {
                throw new IllegalStateException("본인 댓글만 삭제할 수 있습니다.");
            }
        } else {
            if (guestPassword == null || guestPassword.isBlank()) {
                throw new IllegalArgumentException("비밀번호를 입력하세요.");
            }
            if (!passwordEncoder.matches(guestPassword, existing.getGuestPasswordHash())) {
                throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
            }
        }

        // 물리 삭제 대신 soft delete
        commentMapper.softDeleteComment(commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long quizId, Long userId, int page, int size) {
        int pageSafe = Math.max(page, 1);
        int sizeSafe = Math.max(size, 1);
        int offset = (pageSafe - 1) * sizeSafe;

        List<Comment> comments = commentMapper.findCommentsByQuizId(quizId, sizeSafe, offset);

        List<Long> writerIds = comments.stream()
                .map(Comment::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, UserResponse> writers = writerIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.findByIds(writerIds).stream()
                .collect(Collectors.toMap(UserResponse::getId, Function.identity()));

        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .toList();

        Set<Long> likedIds = (userId != null && !commentIds.isEmpty())
                ? new HashSet<>(commentLikeMapper.findLikedCommentIds(userId, commentIds))
                : Collections.emptySet();

        return comments.stream()
                .map(comment -> {
                    UserResponse writer = comment.getUserId() != null
                            ? writers.get(comment.getUserId())
                            : null;

                    String nickname = writer != null ? writer.getNickname() : comment.getGuestNickname();
                    String profileImage = writer != null ? writer.getProfileImageUrl() : null;

                    boolean isOwner = userId != null && comment.getUserId() != null && userId.equals(comment.getUserId());
                    boolean likedByMe = likedIds.contains(comment.getId());

                    return CommentResponse.builder()
                            .id(comment.getId())
                            .quizId(comment.getQuizId())
                            .userId(comment.getUserId())
                            .nickname(nickname)
                            .profileImageUrl(profileImage)

                            .content(comment.getContent())
                            .likeCount(comment.getLikeCount())

                            .mine(isOwner)
                            .likedByMe(likedByMe)

                            .createdAt(comment.getCreatedAt())
                            .updatedAt(comment.getUpdatedAt())
                            .build();
                })
                .toList();
    }


    @Override
    @Transactional
    public void toggleLike(Long commentId, Long userId) {
        if (userId == null) {
            throw new IllegalStateException("로그인 후 이용해주세요.");
        }

        Comment comment = commentMapper.findById(commentId);
        if (comment == null || comment.isDeleted()) {
            throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
        }

        boolean alreadyLiked = commentLikeMapper.exists(userId, commentId);

        if (alreadyLiked) {
            // 좋아요 취소
            commentLikeMapper.delete(userId, commentId);
            commentMapper.decreaseLikeCount(commentId);
        } else {
            // 좋아요 추가
            CommentLike like = new CommentLike();
            like.setUserId(userId);
            like.setCommentId(commentId);
            commentLikeMapper.insert(like);
            commentMapper.increaseLikeCount(commentId);
        }
    }
}
