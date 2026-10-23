package com.projectflow.dto.project;

import com.projectflow.dto.user.UserResponse;
import com.projectflow.entity.Project;
import com.projectflow.entity.ProjectStatus;

import java.time.Instant;
import java.time.LocalDate;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate dueDate,
        UserResponse owner,
        Instant createdAt,
        Instant updatedAt,
        long taskCount,
        long completedTaskCount,
        int progressPercent
) {
    public static ProjectResponse from(Project project, long taskCount, long completedTaskCount) {
        int progress = taskCount == 0 ? 0 : (int) Math.round((completedTaskCount * 100.0) / taskCount);
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getStartDate(),
                project.getDueDate(),
                UserResponse.from(project.getOwner()),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                taskCount,
                completedTaskCount,
                progress
        );
    }
}
