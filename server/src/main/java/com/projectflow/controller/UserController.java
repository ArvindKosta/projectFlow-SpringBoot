package com.projectflow.controller;

import com.projectflow.dto.common.PageResponse;
import com.projectflow.dto.user.CreateUserRequest;
import com.projectflow.dto.user.UpdateUserRequest;
import com.projectflow.dto.user.UserResponse;
import com.projectflow.security.UserPrincipal;
import com.projectflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public PageResponse<UserResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                           @RequestParam(required = false) String q,
                                           @PageableDefault(size = 20) Pageable pageable) {
        return userService.list(principal, q, pageable);
    }

    @GetMapping("/{id}")
    public UserResponse get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return userService.get(principal, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody CreateUserRequest request) {
        return userService.create(principal, request);
    }

    @PatchMapping("/{id}")
    public UserResponse update(@AuthenticationPrincipal UserPrincipal principal,
                               @PathVariable Long id,
                               @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(principal, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        userService.delete(principal, id);
    }
}
