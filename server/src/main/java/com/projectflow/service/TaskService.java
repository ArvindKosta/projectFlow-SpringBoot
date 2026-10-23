package com.projectflow.service;

import com.projectflow.dto.common.PageResponse;
import com.projectflow.dto.task.CreateTaskRequest;
import com.projectflow.dto.task.TaskResponse;
import com.projectflow.dto.task.UpdateTaskRequest;
import com.projectflow.entity.Project;
import com.projectflow.entity.Task;
import com.projectflow.entity.TaskPriority;
import com.projectflow.entity.TaskStatus;
import com.projectflow.entity.User;
import com.projectflow.exception.ApiException;
import com.projectflow.repository.TaskRepository;
import com.projectflow.repository.UserRepository;
import com.projectflow.security.UserPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final AuthorizationService authorizationService;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       ProjectService projectService,
                       AuthorizationService authorizationService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectService = projectService;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> list(UserPrincipal principal, Long projectId, String q, TaskStatus status, Pageable pageable) {
        Project project = projectService.requireProject(projectId);
        authorizationService.requireView(principal, project);
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return PageResponse.from(taskRepository.searchByProject(projectId, status, query, pageable).map(TaskResponse::from));
    }

    @Transactional(readOnly = true)
    public TaskResponse get(UserPrincipal principal, Long id) {
        Task task = requireTask(id);
        authorizationService.requireView(principal, task.getProject());
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse create(UserPrincipal principal, Long projectId, CreateTaskRequest request) {
        Project project = projectService.requireProject(projectId);
        if (!authorizationService.canManageProject(principal, project)) {
            throw authorizationService.forbidden();
        }
        User creator = userRepository.findById(principal.getId()).orElseThrow();
        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setStatus(request.status() == null ? TaskStatus.TODO : request.status());
        task.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());
        task.setDueDate(request.dueDate());
        task.setProject(project);
        task.setCreatedBy(creator);
        task.setAssignee(resolveAssignee(request.assigneeId()));
        taskRepository.save(task);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse update(UserPrincipal principal, Long id, UpdateTaskRequest request) {
        Task task = requireTask(id);
        if (!request.version().equals(task.getVersion())) {
            throw new ApiException(HttpStatus.CONFLICT, "STALE_TASK_VERSION",
                    "This task was updated by someone else. Reload the latest version and try again.");
        }
        boolean full = authorizationService.canManageTaskFully(principal, task);
        boolean limited = authorizationService.canUpdateTask(principal, task);
        if (!full && !limited) {
            throw authorizationService.forbidden();
        }
        if (full) {
            if (request.title() != null) {
                task.setTitle(request.title().trim());
            }
            if (request.description() != null) {
                task.setDescription(request.description());
            }
            if (request.priority() != null) {
                task.setPriority(request.priority());
            }
            if (request.dueDate() != null) {
                task.setDueDate(request.dueDate());
            }
            if (request.clearAssignee()) {
                task.setAssignee(null);
            } else if (request.assigneeId() != null) {
                task.setAssignee(resolveAssignee(request.assigneeId()));
            }
        } else if (request.title() != null || request.description() != null || request.priority() != null
                || request.dueDate() != null || request.assigneeId() != null || request.clearAssignee()) {
            throw authorizationService.forbidden();
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        try {
            taskRepository.flush();
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "STALE_TASK_VERSION",
                    "This task was updated by someone else. Reload the latest version and try again.");
        }
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(UserPrincipal principal, Long id) {
        Task task = requireTask(id);
        if (!authorizationService.canManageTaskFully(principal, task)) {
            throw authorizationService.forbidden();
        }
        taskRepository.delete(task);
    }

    private User resolveAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
                .orElseThrow(() -> authorizationService.notFound("User"));
    }

    public Task requireTask(Long id) {
        return taskRepository.findDetailedById(id)
                .orElseThrow(() -> authorizationService.notFound("Task"));
    }
}
