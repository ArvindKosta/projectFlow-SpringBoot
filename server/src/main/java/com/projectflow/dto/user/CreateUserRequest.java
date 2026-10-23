package com.projectflow.dto.user;

import com.projectflow.entity.GlobalRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull GlobalRole globalRole,
        Boolean active
) {}
