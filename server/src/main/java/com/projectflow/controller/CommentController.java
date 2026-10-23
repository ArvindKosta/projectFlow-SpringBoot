package com.projectflow.controller;

import com.projectflow.dto.comment.CommentRequest;
import com.projectflow.dto.comment.CommentResponse;
import com.projectflow.security.UserPrincipal;
import com.projectflow.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/tasks/{taskId}/comments")
    public List<CommentResponse> list(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long taskId) {
        return commentService.list(principal, taskId);
    }

    @PostMapping("/api/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@AuthenticationPrincipal UserPrincipal principal,
                                  @PathVariable Long taskId,
                                  @Valid @RequestBody CommentRequest request) {
        return commentService.create(principal, taskId, request);
    }

    @PatchMapping("/api/comments/{id}")
    public CommentResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                  @PathVariable Long id,
                                  @Valid @RequestBody CommentRequest request) {
        return commentService.update(principal, id, request);
    }

    @DeleteMapping("/api/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        commentService.delete(principal, id);
    }
}
