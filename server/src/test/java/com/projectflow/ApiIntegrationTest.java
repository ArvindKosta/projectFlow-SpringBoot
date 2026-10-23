package com.projectflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectMemberRepository memberRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User admin;
    private User manager;
    private User member;
    private User viewer;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        admin = user("Admin", "admin@test.dev", "Password123!", GlobalRole.ADMIN);
        manager = user("Manager", "manager@test.dev", "Password123!", GlobalRole.PROJECT_MANAGER);
        member = user("Member", "member@test.dev", "Password123!", GlobalRole.TEAM_MEMBER);
        viewer = user("Viewer", "viewer@test.dev", "Password123!", GlobalRole.VIEWER);
        outsider = user("Outsider", "out@test.dev", "Password123!", GlobalRole.TEAM_MEMBER);

        project = new Project();
        project.setName("Alpha");
        project.setDescription("Test project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(manager);
        projectRepository.save(project);

        addMember(project, manager, ProjectRole.MANAGER);
        addMember(project, member, ProjectRole.MEMBER);
        addMember(project, viewer, ProjectRole.VIEWER);

        task = new Task();
        task.setTitle("Write spec");
        task.setDescription("Initial");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setDueDate(LocalDate.now().plusDays(2));
        task.setProject(project);
        task.setAssignee(member);
        task.setCreatedBy(manager);
        taskRepository.saveAndFlush(task);
    }

    private User outsider;

    @Test
    void loginRejectsBadPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.dev","password":"nope"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerAlwaysCreatesTeamMember() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New User","email":"new@test.dev","password":"Password123!"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.path("user").path("globalRole").asText()).isEqualTo("TEAM_MEMBER");
        assertThat(userRepository.findByEmailIgnoreCase("new@test.dev").orElseThrow().getPasswordHash())
                .doesNotContain("Password123!");
    }

    @Test
    void unauthenticatedApiIsRejected() throws Exception {
        mockMvc.perform(get("/api/projects")).andExpect(status().isUnauthorized());
    }

    @Test
    void viewerCannotCreateProject() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", bearer(viewer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Secret","description":"nope"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void outsiderCannotReadProjectById() throws Exception {
        mockMvc.perform(get("/api/projects/" + project.getId())
                        .header("Authorization", bearer(outsider)))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanCreateAndListProject() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Beta","description":"Second","status":"PLANNING"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Beta"));
        mockMvc.perform(get("/api/projects").header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void duplicateMembershipIsRejected() throws Exception {
        mockMvc.perform(post("/api/projects/" + project.getId() + "/members")
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + member.getId() + ",\"projectRole\":\"MEMBER\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void memberCannotAddMembers() throws Exception {
        mockMvc.perform(post("/api/projects/" + project.getId() + "/members")
                        .header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + outsider.getId() + ",\"projectRole\":\"MEMBER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCanCommentAndViewerCannot() throws Exception {
        mockMvc.perform(post("/api/tasks/" + task.getId() + "/comments")
                        .header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Looks good\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/tasks/" + task.getId() + "/comments")
                        .header("Authorization", bearer(viewer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"I should not post\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validationErrorsReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void staleTaskUpdateReturns409AndDoesNotOverwrite() throws Exception {
        String token = bearer(manager);
        long initialVersion = task.getVersion();
        mockMvc.perform(patch("/api/tasks/" + task.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + initialVersion + ",\"title\":\"First write\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First write"))
                .andExpect(jsonPath("$.version").value(initialVersion + 1));

        mockMvc.perform(patch("/api/tasks/" + task.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + initialVersion + ",\"title\":\"Stale write\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STALE_TASK_VERSION"));

        mockMvc.perform(get("/api/tasks/" + task.getId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First write"));
    }

    @Test
    void adminCanManageUsers() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Pat","email":"pat@test.dev","password":"Password123!","globalRole":"TEAM_MEMBER","active":true}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/users?q=pat").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void memberCannotCreateTask() throws Exception {
        mockMvc.perform(post("/api/projects/" + project.getId() + "/tasks")
                        .header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Nope\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void assigneeCanUpdateStatusOnly() throws Exception {
        long initialVersion = task.getVersion();
        mockMvc.perform(patch("/api/tasks/" + task.getId())
                        .header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + initialVersion + ",\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        mockMvc.perform(patch("/api/tasks/" + task.getId())
                        .header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + (initialVersion + 1) + ",\"title\":\"Hijack\"}"))
                .andExpect(status().isForbidden());
    }

    private String bearer(User user) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + user.getEmail() + "\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer " + objectMapper.readTree(result.getResponse().getContentAsString()).path("accessToken").asText();
    }

    private User user(String name, String email, String password, GlobalRole role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setGlobalRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    private void addMember(Project project, User user, ProjectRole role) {
        ProjectMember membership = new ProjectMember();
        membership.setProject(project);
        membership.setUser(user);
        membership.setProjectRole(role);
        memberRepository.save(membership);
    }
}
