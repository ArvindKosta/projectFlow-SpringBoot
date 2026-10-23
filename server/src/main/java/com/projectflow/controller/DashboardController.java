package com.projectflow.controller;

import com.projectflow.dto.dashboard.DashboardStatsResponse;
import com.projectflow.security.UserPrincipal;
import com.projectflow.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public DashboardStatsResponse stats(@AuthenticationPrincipal UserPrincipal principal) {
        return dashboardService.stats(principal);
    }
}
