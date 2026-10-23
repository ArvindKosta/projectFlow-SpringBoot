package com.projectflow.service;

import com.projectflow.entity.GlobalRole;
import com.projectflow.entity.Project;
import com.projectflow.entity.ProjectMember;
import com.projectflow.entity.ProjectRole;
import com.projectflow.entity.Task;
import com.projectflow.entity.User;
import com.projectflow.exception.ApiException;
import com.projectflow.repository.ProjectMemberRepository;
import com.projectflow.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthorizationService {

    private final ProjectMemberRepository projectMemberRepository;

    public AuthorizationService(ProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }

    public boolean isAdmin(UserPrincipal principal) {
        return principal.getGlobalRole() == GlobalRole.ADMIN;
    }

    public boolean isAdmin(User user) {
        return user.getGlobalRole() == GlobalRole.ADMIN;
    }

    public Optional<ProjectMember> membership(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
    }

    public boolean canViewProject(UserPrincipal principal, Project project) {
        if (isAdmin(principal)) {
            return true;
        }
        if (project.getOwner().getId().equals(principal.getId())) {
            return true;
        }
        return membership(project.getId(), principal.getId()).isPresent();
    }

    public boolean canManageProject(UserPrincipal principal, Project project) {
        if (isAdmin(principal)) {
            return true;
        }
        if (project.getOwner().getId().equals(principal.getId())) {
            return true;
        }
        return membership(project.getId(), principal.getId())
                .filter(m -> m.getProjectRole() == ProjectRole.MANAGER)
                .isPresent();
    }

    public boolean canCreateProject(UserPrincipal principal) {
        return principal.getGlobalRole() == GlobalRole.ADMIN
                || principal.getGlobalRole() == GlobalRole.PROJECT_MANAGER;
    }

    public boolean canMutateAsMember(UserPrincipal principal, Project project) {
        if (canManageProject(principal, project)) {
            return true;
        }
        if (principal.getGlobalRole() == GlobalRole.VIEWER) {
            return false;
        }
        return membership(project.getId(), principal.getId())
                .filter(m -> m.getProjectRole() == ProjectRole.MEMBER)
                .isPresent();
    }

    public boolean canUpdateTask(UserPrincipal principal, Task task) {
        if (canManageProject(principal, task.getProject())) {
            return true;
        }
        if (principal.getGlobalRole() == GlobalRole.VIEWER) {
            return false;
        }
        Optional<ProjectMember> member = membership(task.getProject().getId(), principal.getId());
        if (member.isEmpty() || member.get().getProjectRole() == ProjectRole.VIEWER) {
            return false;
        }
        return task.getAssignee() != null && task.getAssignee().getId().equals(principal.getId());
    }

    public boolean canManageTaskFully(UserPrincipal principal, Task task) {
        return canManageProject(principal, task.getProject());
    }

    public boolean canComment(UserPrincipal principal, Project project) {
        if (principal.getGlobalRole() == GlobalRole.VIEWER) {
            return membership(project.getId(), principal.getId())
                    .filter(m -> m.getProjectRole() != ProjectRole.VIEWER)
                    .isPresent() || isAdmin(principal);
        }
        if (isAdmin(principal) || project.getOwner().getId().equals(principal.getId())) {
            return true;
        }
        return membership(project.getId(), principal.getId())
                .filter(m -> m.getProjectRole() != ProjectRole.VIEWER)
                .isPresent();
    }

    public void requireView(UserPrincipal principal, Project project) {
        if (!canViewProject(principal, project)) {
            throw forbidden();
        }
    }

    public void requireManage(UserPrincipal principal, Project project) {
        if (!canManageProject(principal, project)) {
            throw forbidden();
        }
    }

    public ApiException forbidden() {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You are not allowed to perform this action.");
    }

    public ApiException notFound(String resource) {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", resource + " was not found.");
    }
}
