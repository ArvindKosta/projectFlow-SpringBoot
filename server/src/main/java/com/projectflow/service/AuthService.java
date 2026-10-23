package com.projectflow.service;

import com.projectflow.config.AppProperties;
import com.projectflow.dto.auth.AuthResponse;
import com.projectflow.dto.auth.LoginRequest;
import com.projectflow.dto.auth.RegisterRequest;
import com.projectflow.dto.user.UpdateProfileRequest;
import com.projectflow.dto.user.UserResponse;
import com.projectflow.entity.GlobalRole;
import com.projectflow.entity.User;
import com.projectflow.exception.ApiException;
import com.projectflow.repository.UserRepository;
import com.projectflow.security.JwtService;
import com.projectflow.security.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       CookieService cookieService,
                       AppProperties appProperties,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.cookieService = cookieService;
        this.appProperties = appProperties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);
        cookieService.setAccessToken(response, token);
        User user = userRepository.findById(principal.getId()).orElseThrow();
        return new AuthResponse(UserResponse.from(user), "Bearer", appProperties.getJwt().getExpirationMs(), token);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "An account with this email already exists.");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setGlobalRole(GlobalRole.TEAM_MEMBER);
        user.setActive(true);
        userRepository.save(user);
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);
        cookieService.setAccessToken(response, token);
        return new AuthResponse(UserResponse.from(user), "Bearer", appProperties.getJwt().getExpirationMs(), token);
    }

    public void logout(HttpServletResponse response) {
        cookieService.clearAccessToken(response);
    }

    @Transactional(readOnly = true)
    public UserResponse me(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User was not found."));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(UserPrincipal principal, UpdateProfileRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User was not found."));
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", "Current password is incorrect.");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        return UserResponse.from(user);
    }
}
