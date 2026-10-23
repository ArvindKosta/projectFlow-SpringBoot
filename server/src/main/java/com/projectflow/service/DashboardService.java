package com.projectflow.service;

import com.projectflow.dto.dashboard.DashboardStatsResponse;
import com.projectflow.dto.project.ProjectResponse;
import com.projectflow.dto.task.TaskResponse;
import com.projectflow.entity.Project;
import com.projectflow.entity.ProjectStatus;
import com.projectflow.entity.TaskStatus;
import com.projectflow.repository.ProjectMemberRepository;
import com.projectflow.repository.ProjectRepository;
import com.projectflow.repository.TaskRepository;
import com.projectflow.security.UserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final AuthorizationService authorizationService;

    public DashboardService(ProjectRepository projectRepository,
                            ProjectMemberRepository projectMemberRepository,
                            TaskRepository taskRepository,
                            ProjectService projectService,
                            AuthorizationService authorizationService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse stats(UserPrincipal principal) {
        boolean admin = authorizationService.isAdmin(principal);
        Set<Long> projectIds = accessibleProjectIds(principal, admin);
        long totalProjects = projectRepository.countAccessible(principal.getId(), admin);
        long activeProjects = projectRepository.countAccessibleByStatus(principal.getId(), admin, ProjectStatus.ACTIVE);
        List<Long> ids = new ArrayList<>(projectIds);
        long totalTasks = ids.isEmpty() ? 0 : taskRepository.countByProjectIdIn(ids);
        long completed = ids.isEmpty() ? 0 : taskRepository.countByProjectIdInAndStatus(ids, TaskStatus.DONE);
        long overdue = ids.isEmpty() ? 0 : taskRepository.countOverdue(ids, LocalDate.now());
        long pending = Math.max(0, totalTasks - completed);
        List<ProjectResponse> recentProjects = projectRepository
                .findRecentAccessible(principal.getId(), admin, PageRequest.of(0, 5))
                .stream()
                .map(projectService::toResponse)
                .toList();
        List<TaskResponse> recentTasks = ids.isEmpty()
                ? List.of()
                : taskRepository.findRecentByProjectIds(ids, PageRequest.of(0, 5)).stream()
                .map(TaskResponse::from)
                .toList();
        return new DashboardStatsResponse(totalProjects, activeProjects, totalTasks, completed, pending, overdue, recentProjects, recentTasks);
    }

    private Set<Long> accessibleProjectIds(UserPrincipal principal, boolean admin) {
        Set<Long> ids = new LinkedHashSet<>();
        if (admin) {
            projectRepository.findAll().forEach(p -> ids.add(p.getId()));
            return ids;
        }
        projectRepository.findRecentAccessible(principal.getId(), false, PageRequest.of(0, 1000))
                .forEach(p -> ids.add(p.getId()));
        projectMemberRepository.findByUserId(principal.getId())
                .forEach(m -> ids.add(m.getProject().getId()));
        return ids;
    }
}
