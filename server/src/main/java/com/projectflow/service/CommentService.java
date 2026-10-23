package com.projectflow.service;

import com.projectflow.dto.comment.CommentRequest;
import com.projectflow.dto.comment.CommentResponse;
import com.projectflow.entity.Comment;
import com.projectflow.entity.Task;
import com.projectflow.entity.User;
import com.projectflow.repository.CommentRepository;
import com.projectflow.repository.UserRepository;
import com.projectflow.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TaskService taskService;
    private final AuthorizationService authorizationService;

    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository,
                          TaskService taskService,
                          AuthorizationService authorizationService) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.taskService = taskService;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(UserPrincipal principal, Long taskId) {
        Task task = taskService.requireTask(taskId);
        authorizationService.requireView(principal, task.getProject());
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse create(UserPrincipal principal, Long taskId, CommentRequest request) {
        Task task = taskService.requireTask(taskId);
        if (!authorizationService.canComment(principal, task.getProject())) {
            throw authorizationService.forbidden();
        }
        User author = userRepository.findById(principal.getId()).orElseThrow();
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setContent(request.content().trim());
        commentRepository.save(comment);
        return CommentResponse.from(comment);
    }

    @Transactional
    public CommentResponse update(UserPrincipal principal, Long id, CommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> authorizationService.notFound("Comment"));
        authorizationService.requireView(principal, comment.getTask().getProject());
        boolean author = comment.getAuthor().getId().equals(principal.getId());
        if (!author && !authorizationService.isAdmin(principal)) {
            throw authorizationService.forbidden();
        }
        comment.setContent(request.content().trim());
        return CommentResponse.from(comment);
    }

    @Transactional
    public void delete(UserPrincipal principal, Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> authorizationService.notFound("Comment"));
        authorizationService.requireView(principal, comment.getTask().getProject());
        boolean author = comment.getAuthor().getId().equals(principal.getId());
        boolean manager = authorizationService.canManageProject(principal, comment.getTask().getProject());
        if (!author && !manager) {
            throw authorizationService.forbidden();
        }
        commentRepository.delete(comment);
    }
}
