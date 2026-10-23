package com.projectflow.repository;

import com.projectflow.entity.ProjectMember;
import com.projectflow.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectIdOrderByJoinedAtAsc(Long projectId);
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
    Optional<ProjectMember> findByIdAndProjectId(Long id, Long projectId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    long countByProjectIdAndProjectRole(Long projectId, ProjectRole role);
    void deleteByProjectId(Long projectId);
    List<ProjectMember> findByUserId(Long userId);
}
