package com.projectflow.dto.project;

import com.projectflow.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record MemberRequest(
        @NotNull Long userId,
        @NotNull ProjectRole projectRole
) {}
