package com.projectflow.dto.project;

import com.projectflow.dto.user.UserResponse;
import com.projectflow.entity.ProjectMember;
import com.projectflow.entity.ProjectRole;

import java.time.Instant;

public record MemberResponse(
        Long id,
        UserResponse user,
        ProjectRole projectRole,
        Instant joinedAt
) {
    public static MemberResponse from(ProjectMember member) {
        return new MemberResponse(
                member.getId(),
                UserResponse.from(member.getUser()),
                member.getProjectRole(),
                member.getJoinedAt()
        );
    }
}
