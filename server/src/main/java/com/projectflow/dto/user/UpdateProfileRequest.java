package com.projectflow.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 120) String name,
        @Size(min = 8, max = 72) String currentPassword,
        @Size(min = 8, max = 72) String newPassword
) {}
