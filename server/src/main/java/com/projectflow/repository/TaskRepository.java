package com.projectflow.repository;

import com.projectflow.entity.Task;
import com.projectflow.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"project", "assignee", "createdBy"})
    Optional<Task> findDetailedById(Long id);

    @Query("""
            SELECT t FROM Task t
            WHERE t.project.id = :projectId
              AND (:status IS NULL OR t.status = :status)
              AND (:q IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    @EntityGraph(attributePaths = {"project", "assignee", "createdBy"})
    Page<Task> searchByProject(
            @Param("projectId") Long projectId,
            @Param("status") TaskStatus status,
            @Param("q") String q,
            Pageable pageable);

    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.project.id IN :projectIds
            """)
    long countByProjectIdIn(@Param("projectIds") Collection<Long> projectIds);

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.project.id IN :projectIds AND t.status = :status
            """)
    long countByProjectIdInAndStatus(@Param("projectIds") Collection<Long> projectIds, @Param("status") TaskStatus status);

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.project.id IN :projectIds
              AND t.status <> com.projectflow.entity.TaskStatus.DONE
              AND t.dueDate < :today
            """)
    long countOverdue(@Param("projectIds") Collection<Long> projectIds, @Param("today") LocalDate today);

    @Query("""
            SELECT t FROM Task t
            WHERE t.project.id IN :projectIds
            ORDER BY t.updatedAt DESC
            """)
    List<Task> findRecentByProjectIds(@Param("projectIds") Collection<Long> projectIds, Pageable pageable);

    @Query("""
            SELECT t.project.id FROM Task t WHERE t.project.id IN :projectIds
            """)
    List<Long> findProjectIdsWithTasks(@Param("projectIds") Collection<Long> projectIds);

    @Query("SELECT DISTINCT t.project.id FROM Task t JOIN t.project p LEFT JOIN ProjectMember pm ON pm.project = p WHERE (:admin = true) OR p.owner.id = :userId OR pm.user.id = :userId")
    List<Long> findAccessibleProjectIds(@Param("userId") Long userId, @Param("admin") boolean admin);
}
