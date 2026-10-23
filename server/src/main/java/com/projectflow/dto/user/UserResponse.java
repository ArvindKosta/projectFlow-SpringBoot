package com.projectflow.dto.user;

import com.projectflow.entity.GlobalRole;
import com.projectflow.entity.User;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        GlobalRole globalRole,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getGlobalRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
