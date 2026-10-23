package com.projectflow.dto.comment;

import com.projectflow.dto.user.UserResponse;
import com.projectflow.entity.Comment;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        Long taskId,
        UserResponse author,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getTask().getId(),
                UserResponse.from(comment.getAuthor()),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
