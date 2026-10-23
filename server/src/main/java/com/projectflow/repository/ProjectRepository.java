package com.projectflow.repository;

import com.projectflow.entity.Project;
import com.projectflow.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
            SELECT DISTINCT p FROM Project p
            LEFT JOIN ProjectMember pm ON pm.project = p
            WHERE (:admin = true)
               OR p.owner.id = :userId
               OR pm.user.id = :userId
            """)
    Page<Project> findAccessible(@Param("userId") Long userId, @Param("admin") boolean admin, Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Project p
            LEFT JOIN ProjectMember pm ON pm.project = p
            WHERE ((:admin = true) OR p.owner.id = :userId OR pm.user.id = :userId)
              AND (:status IS NULL OR p.status = :status)
              AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Project> searchAccessible(
            @Param("userId") Long userId,
            @Param("admin") boolean admin,
            @Param("status") ProjectStatus status,
            @Param("q") String q,
            Pageable pageable);

    @Query("""
            SELECT COUNT(DISTINCT p) FROM Project p
            LEFT JOIN ProjectMember pm ON pm.project = p
            WHERE (:admin = true) OR p.owner.id = :userId OR pm.user.id = :userId
            """)
    long countAccessible(@Param("userId") Long userId, @Param("admin") boolean admin);

    @Query("""
            SELECT COUNT(DISTINCT p) FROM Project p
            LEFT JOIN ProjectMember pm ON pm.project = p
            WHERE p.status = :status
              AND ((:admin = true) OR p.owner.id = :userId OR pm.user.id = :userId)
            """)
    long countAccessibleByStatus(@Param("userId") Long userId, @Param("admin") boolean admin, @Param("status") ProjectStatus status);

    @Query("""
            SELECT DISTINCT p FROM Project p
            LEFT JOIN ProjectMember pm ON pm.project = p
            WHERE (:admin = true) OR p.owner.id = :userId OR pm.user.id = :userId
            ORDER BY p.updatedAt DESC
            """)
    List<Project> findRecentAccessible(@Param("userId") Long userId, @Param("admin") boolean admin, Pageable pageable);
}
