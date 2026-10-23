package com.projectflow.dto.auth;

import com.projectflow.dto.user.UserResponse;

public record AuthResponse(UserResponse user, String tokenType, long expiresInMs, String accessToken) {}
