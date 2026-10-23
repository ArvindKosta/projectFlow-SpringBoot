package com.projectflow.service;

import com.projectflow.dto.common.PageResponse;
import com.projectflow.dto.project.MemberRequest;
import com.projectflow.dto.project.MemberResponse;
import com.projectflow.dto.project.ProjectRequest;
import com.projectflow.dto.project.ProjectResponse;
import com.projectflow.dto.project.UpdateMemberRequest;
import com.projectflow.entity.Project;
import com.projectflow.entity.ProjectMember;
import com.projectflow.entity.ProjectRole;
import com.projectflow.entity.ProjectStatus;
import com.projectflow.entity.TaskStatus;
import com.projectflow.entity.User;
import com.projectflow.exception.ApiException;
import com.projectflow.repository.ProjectMemberRepository;
import com.projectflow.repository.ProjectRepository;
import com.projectflow.repository.TaskRepository;
import com.projectflow.repository.UserRepository;
import com.projectflow.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          TaskRepository taskRepository,
                          UserRepository userRepository,
                          AuthorizationService authorizationService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> list(UserPrincipal principal, String q, ProjectStatus status, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<Project> page = projectRepository.searchAccessible(principal.getId(), authorizationService.isAdmin(principal), status, query, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(UserPrincipal principal, Long id) {
        Project project = requireProject(id);
        authorizationService.requireView(principal, project);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse create(UserPrincipal principal, ProjectRequest request) {
        if (!authorizationService.canCreateProject(principal)) {
            throw authorizationService.forbidden();
        }
        User owner = userRepository.findById(principal.getId()).orElseThrow();
        Project project = new Project();
        apply(project, request);
        project.setOwner(owner);
        projectRepository.save(project);
        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(project);
        ownerMember.setUser(owner);
        ownerMember.setProjectRole(ProjectRole.MANAGER);
        projectMemberRepository.save(ownerMember);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse update(UserPrincipal principal, Long id, ProjectRequest request) {
        Project project = requireProject(id);
        authorizationService.requireManage(principal, project);
        apply(project, request);
        return toResponse(project);
    }

    @Transactional
    public void delete(UserPrincipal principal, Long id) {
        Project project = requireProject(id);
        authorizationService.requireManage(principal, project);
        projectMemberRepository.deleteByProjectId(id);
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> members(UserPrincipal principal, Long projectId) {
        Project project = requireProject(projectId);
        authorizationService.requireView(principal, project);
        return projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(projectId).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse addMember(UserPrincipal principal, Long projectId, MemberRequest request) {
        Project project = requireProject(projectId);
        authorizationService.requireManage(principal, project);
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_MEMBER", "That user is already a project member.");
        }
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> authorizationService.notFound("User"));
        if (!user.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INACTIVE_USER", "Cannot add an inactive user.");
        }
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setProjectRole(request.projectRole());
        projectMemberRepository.save(member);
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateMember(UserPrincipal principal, Long projectId, Long memberId, UpdateMemberRequest request) {
        Project project = requireProject(projectId);
        authorizationService.requireManage(principal, project);
        ProjectMember member = projectMemberRepository.findByIdAndProjectId(memberId, projectId)
                .orElseThrow(() -> authorizationService.notFound("Member"));
        member.setProjectRole(request.projectRole());
        return MemberResponse.from(member);
    }

    @Transactional
    public void removeMember(UserPrincipal principal, Long projectId, Long memberId) {
        Project project = requireProject(projectId);
        authorizationService.requireManage(principal, project);
        ProjectMember member = projectMemberRepository.findByIdAndProjectId(memberId, projectId)
                .orElseThrow(() -> authorizationService.notFound("Member"));
        if (member.getUser().getId().equals(project.getOwner().getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OWNER_MEMBER", "The project owner cannot be removed.");
        }
        projectMemberRepository.delete(member);
    }

    public Project requireProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> authorizationService.notFound("Project"));
    }

    private void apply(Project project, ProjectRequest request) {
        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setStatus(request.status() == null ? ProjectStatus.PLANNING : request.status());
        project.setStartDate(request.startDate());
        project.setDueDate(request.dueDate());
    }

    public ProjectResponse toResponse(Project project) {
        long total = taskRepository.countByProjectId(project.getId());
        long done = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
        return ProjectResponse.from(project, total, done);
    }
}
