package com.projectflow.dto.dashboard;

import com.projectflow.dto.project.ProjectResponse;
import com.projectflow.dto.task.TaskResponse;

import java.util.List;

public record DashboardStatsResponse(
        long totalProjects,
        long activeProjects,
        long totalTasks,
        long completedTasks,
        long pendingTasks,
        long overdueTasks,
        List<ProjectResponse> recentProjects,
        List<TaskResponse> recentTasks
) {}
