package com.projectflow.dto.project;

import com.projectflow.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 4000) String description,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate dueDate
) {}
