package com.projectflow.config;

import com.projectflow.entity.GlobalRole;
import com.projectflow.entity.Project;
import com.projectflow.entity.ProjectMember;
import com.projectflow.entity.ProjectRole;
import com.projectflow.entity.ProjectStatus;
import com.projectflow.entity.Task;
import com.projectflow.entity.TaskPriority;
import com.projectflow.entity.TaskStatus;
import com.projectflow.entity.User;
import com.projectflow.repository.ProjectMemberRepository;
import com.projectflow.repository.ProjectRepository;
import com.projectflow.repository.TaskRepository;
import com.projectflow.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@Profile("dev")
public class DevDataSeeder {

    @Bean
    CommandLineRunner seedDevelopmentData(UserRepository users,
                                          ProjectRepository projects,
                                          ProjectMemberRepository members,
                                          TaskRepository tasks,
                                          PasswordEncoder encoder) {
        return args -> {
            if (users.count() > 0) {
                return;
            }
            User admin = saveUser(users, encoder, "Ada Admin", "admin@projectflow.dev", "Admin123!", GlobalRole.ADMIN);
            User manager = saveUser(users, encoder, "Morgan Manager", "manager@projectflow.dev", "Manager123!", GlobalRole.PROJECT_MANAGER);
            User member = saveUser(users, encoder, "Taylor Member", "member@projectflow.dev", "Member123!", GlobalRole.TEAM_MEMBER);
            User viewer = saveUser(users, encoder, "Riley Viewer", "viewer@projectflow.dev", "Viewer123!", GlobalRole.VIEWER);

            Project website = new Project();
            website.setName("Website Relaunch");
            website.setDescription("Development-only sample project for the marketing site relaunch.");
            website.setStatus(ProjectStatus.ACTIVE);
            website.setStartDate(LocalDate.now().minusDays(10));
            website.setDueDate(LocalDate.now().plusDays(20));
            website.setOwner(manager);
            projects.save(website);

            saveMember(members, website, manager, ProjectRole.MANAGER);
            saveMember(members, website, member, ProjectRole.MEMBER);
            saveMember(members, website, viewer, ProjectRole.VIEWER);

            Task design = new Task();
            design.setTitle("Finalize homepage wireframes");
            design.setDescription("Sample task used only in local development.");
            design.setStatus(TaskStatus.IN_PROGRESS);
            design.setPriority(TaskPriority.HIGH);
            design.setDueDate(LocalDate.now().plusDays(3));
            design.setProject(website);
            design.setAssignee(member);
            design.setCreatedBy(manager);
            tasks.save(design);

            Task overdue = new Task();
            overdue.setTitle("Publish launch checklist");
            overdue.setDescription("Overdue sample task for dashboard statistics.");
            overdue.setStatus(TaskStatus.TODO);
            overdue.setPriority(TaskPriority.URGENT);
            overdue.setDueDate(LocalDate.now().minusDays(2));
            overdue.setProject(website);
            overdue.setAssignee(member);
            overdue.setCreatedBy(manager);
            tasks.save(overdue);

            Task done = new Task();
            done.setTitle("Kickoff notes");
            done.setDescription("Completed sample task.");
            done.setStatus(TaskStatus.DONE);
            done.setPriority(TaskPriority.MEDIUM);
            done.setDueDate(LocalDate.now().minusDays(5));
            done.setProject(website);
            done.setAssignee(admin);
            done.setCreatedBy(manager);
            tasks.save(done);
        };
    }

    private User saveUser(UserRepository users, PasswordEncoder encoder, String name, String email, String password, GlobalRole role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));
        user.setGlobalRole(role);
        user.setActive(true);
        return users.save(user);
    }

    private void saveMember(ProjectMemberRepository members, Project project, User user, ProjectRole role) {
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setProjectRole(role);
        members.save(member);
    }
}
