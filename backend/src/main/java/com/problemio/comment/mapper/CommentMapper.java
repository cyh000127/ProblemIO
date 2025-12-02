package com.problemio.comment.mapper;

import com.problemio.comment.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    void insertComment(Comment comment);

    void updateComment(Comment comment);

    void softDeleteComment(@Param("id") Long id);

    Comment findById(@Param("id") Long id);

    List<Comment> findCommentsByQuizId(
            @Param("quizId") Long quizId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    int countCommentsByQuizId(@Param("quizId") Long quizId);

    void increaseLikeCount(@Param("commentId") Long commentId);

    void decreaseLikeCount(@Param("commentId") Long commentId);
}
