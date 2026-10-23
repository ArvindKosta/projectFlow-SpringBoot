package com.projectflow.dto.user;

import com.projectflow.entity.GlobalRole;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 2, max = 120) String name,
        GlobalRole globalRole,
        Boolean active
) {}
