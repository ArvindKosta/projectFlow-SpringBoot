package com.projectflow.dto.project;

import com.projectflow.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRequest(@NotNull ProjectRole projectRole) {}
