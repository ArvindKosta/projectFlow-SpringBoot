package com.projectflow.service;

import com.projectflow.dto.common.PageResponse;
import com.projectflow.dto.user.CreateUserRequest;
import com.projectflow.dto.user.UpdateUserRequest;
import com.projectflow.dto.user.UserResponse;
import com.projectflow.entity.GlobalRole;
import com.projectflow.entity.User;
import com.projectflow.exception.ApiException;
import com.projectflow.repository.UserRepository;
import com.projectflow.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationService authorizationService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthorizationService authorizationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(UserPrincipal principal, String q, Pageable pageable) {
        if (principal.getGlobalRole() != GlobalRole.ADMIN && principal.getGlobalRole() != GlobalRole.PROJECT_MANAGER) {
            throw authorizationService.forbidden();
        }
        Page<User> page;
        if (q == null || q.isBlank()) {
            page = userRepository.findAll(pageable);
        } else {
            String term = q.trim();
            page = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term, pageable);
        }
        return PageResponse.from(page.map(UserResponse::from));
    }

    @Transactional(readOnly = true)
    public UserResponse get(UserPrincipal principal, Long id) {
        if (principal.getGlobalRole() != GlobalRole.ADMIN && !principal.getId().equals(id)
                && principal.getGlobalRole() != GlobalRole.PROJECT_MANAGER) {
            throw authorizationService.forbidden();
        }
        return UserResponse.from(requireUser(id));
    }

    @Transactional
    public UserResponse create(UserPrincipal principal, CreateUserRequest request) {
        requireAdmin(principal);
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "An account with this email already exists.");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setGlobalRole(request.globalRole());
        user.setActive(request.active() == null || request.active());
        userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse update(UserPrincipal principal, Long id, UpdateUserRequest request) {
        requireAdmin(principal);
        User user = requireUser(id);
        if (request.name() != null) {
            user.setName(request.name().trim());
        }
        if (request.globalRole() != null) {
            if (user.getGlobalRole() == GlobalRole.ADMIN && request.globalRole() != GlobalRole.ADMIN
                    && userRepository.countByGlobalRole(GlobalRole.ADMIN) <= 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "LAST_ADMIN", "Cannot demote the last administrator.");
            }
            user.setGlobalRole(request.globalRole());
        }
        if (request.active() != null) {
            if (!request.active() && user.getGlobalRole() == GlobalRole.ADMIN
                    && userRepository.countByGlobalRole(GlobalRole.ADMIN) <= 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "LAST_ADMIN", "Cannot deactivate the last administrator.");
            }
            user.setActive(request.active());
        }
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(UserPrincipal principal, Long id) {
        requireAdmin(principal);
        User user = requireUser(id);
        if (user.getGlobalRole() == GlobalRole.ADMIN && userRepository.countByGlobalRole(GlobalRole.ADMIN) <= 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LAST_ADMIN", "Cannot delete the last administrator.");
        }
        if (principal.getId().equals(id)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SELF_DELETE", "You cannot delete your own account.");
        }
        user.setActive(false);
    }

    private void requireAdmin(UserPrincipal principal) {
        if (!authorizationService.isAdmin(principal)) {
            throw authorizationService.forbidden();
        }
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> authorizationService.notFound("User"));
    }
}
