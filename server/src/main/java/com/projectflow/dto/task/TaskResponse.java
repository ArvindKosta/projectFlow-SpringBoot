package com.projectflow.dto.task;

import com.projectflow.dto.user.UserResponse;
import com.projectflow.entity.Task;
import com.projectflow.entity.TaskPriority;
import com.projectflow.entity.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long projectId,
        String projectName,
        UserResponse assignee,
        UserResponse createdBy,
        Long version,
        boolean overdue,
        Instant createdAt,
        Instant updatedAt
) {
    public static TaskResponse from(Task task) {
        boolean overdue = task.getDueDate() != null
                && task.getStatus() != TaskStatus.DONE
                && task.getDueDate().isBefore(LocalDate.now());
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getAssignee() == null ? null : UserResponse.from(task.getAssignee()),
                UserResponse.from(task.getCreatedBy()),
                task.getVersion(),
                overdue,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
