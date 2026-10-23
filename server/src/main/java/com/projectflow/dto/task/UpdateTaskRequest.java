package com.projectflow.dto.task;

import com.projectflow.entity.TaskPriority;
import com.projectflow.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(
        @NotNull Long version,
        @Size(max = 200) String title,
        @Size(max = 8000) String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long assigneeId,
        boolean clearAssignee
) {}
