package com.projectflow.controller;

import com.projectflow.dto.common.PageResponse;
import com.projectflow.dto.task.CreateTaskRequest;
import com.projectflow.dto.task.TaskResponse;
import com.projectflow.dto.task.UpdateTaskRequest;
import com.projectflow.entity.TaskStatus;
import com.projectflow.security.UserPrincipal;
import com.projectflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/api/projects/{projectId}/tasks")
    public PageResponse<TaskResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                           @PathVariable Long projectId,
                                           @RequestParam(required = false) String q,
                                           @RequestParam(required = false) TaskStatus status,
                                           @PageableDefault(size = 20) Pageable pageable) {
        return taskService.list(principal, projectId, q, status, pageable);
    }

    @PostMapping("/api/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@AuthenticationPrincipal UserPrincipal principal,
                               @PathVariable Long projectId,
                               @Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(principal, projectId, request);
    }

    @GetMapping("/api/tasks/{id}")
    public TaskResponse get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return taskService.get(principal, id);
    }

    @PatchMapping("/api/tasks/{id}")
    public TaskResponse update(@AuthenticationPrincipal UserPrincipal principal,
                               @PathVariable Long id,
                               @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(principal, id, request);
    }

    @DeleteMapping("/api/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        taskService.delete(principal, id);
    }
}
