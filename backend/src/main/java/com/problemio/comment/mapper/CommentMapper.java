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

    void updateRootCommentId(@Param("id") Long id, @Param("rootCommentId") Long rootCommentId);

    Comment findById(@Param("id") Long id);

    List<Comment> findRootCommentsByQuizId(
            @Param("quizId") Long quizId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    int countCommentsByQuizId(@Param("quizId") Long quizId);

    List<CommentReplyCount> countRepliesByParentIds(@Param("parentIds") List<Long> parentIds);

    void increaseLikeCount(@Param("commentId") Long commentId);

    void decreaseLikeCount(@Param("commentId") Long commentId);

    class CommentReplyCount {
        private Long parentId;
        private Integer count;

        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}
