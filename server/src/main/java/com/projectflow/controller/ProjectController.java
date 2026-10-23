package com.projectflow.controller;

import com.projectflow.dto.common.PageResponse;
import com.projectflow.dto.project.MemberRequest;
import com.projectflow.dto.project.MemberResponse;
import com.projectflow.dto.project.ProjectRequest;
import com.projectflow.dto.project.ProjectResponse;
import com.projectflow.dto.project.UpdateMemberRequest;
import com.projectflow.entity.ProjectStatus;
import com.projectflow.security.UserPrincipal;
import com.projectflow.service.ProjectService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public PageResponse<ProjectResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                              @RequestParam(required = false) String q,
                                              @RequestParam(required = false) ProjectStatus status,
                                              @PageableDefault(size = 12) Pageable pageable) {
        return projectService.list(principal, q, status, pageable);
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return projectService.get(principal, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@AuthenticationPrincipal UserPrincipal principal,
                                  @Valid @RequestBody ProjectRequest request) {
        return projectService.create(principal, request);
    }

    @PatchMapping("/{id}")
    public ProjectResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                  @PathVariable Long id,
                                  @Valid @RequestBody ProjectRequest request) {
        return projectService.update(principal, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        projectService.delete(principal, id);
    }

    @GetMapping("/{projectId}/members")
    public List<MemberResponse> members(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long projectId) {
        return projectService.members(principal, projectId);
    }

    @PostMapping("/{projectId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse addMember(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable Long projectId,
                                    @Valid @RequestBody MemberRequest request) {
        return projectService.addMember(principal, projectId, request);
    }

    @PatchMapping("/{projectId}/members/{memberId}")
    public MemberResponse updateMember(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long projectId,
                                       @PathVariable Long memberId,
                                       @Valid @RequestBody UpdateMemberRequest request) {
        return projectService.updateMember(principal, projectId, memberId, request);
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@AuthenticationPrincipal UserPrincipal principal,
                             @PathVariable Long projectId,
                             @PathVariable Long memberId) {
        projectService.removeMember(principal, projectId, memberId);
    }
}
