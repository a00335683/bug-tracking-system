package com.tus.services;

import com.tus.db.models.Issue;
import com.tus.db.models.Project;
import com.tus.db.models.User;
import com.tus.db.repos.IssueRepository;
import com.tus.db.repos.ProjectRepository;
import com.tus.db.repos.UserRepository;
import com.tus.enums.IssuePriority;
import com.tus.enums.IssueStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class IssueServiceTest {

    @Autowired
    private IssueService issueService;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private Project project;
    private User tester;
    private User developer;

    @BeforeEach
    void setup() {

        issueRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        project = new Project("Test Project", "Testing project");
        projectRepository.save(project);

        tester = new User("tester1", "pass", "TESTER", true);
        developer = new User("dev1", "pass", "DEVELOPER", true);

        userRepository.save(tester);
        userRepository.save(developer);
    }

    // -------------------------
    // Authentication helpers
    // -------------------------

    private void setDeveloperAuth(String username) {
        var auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void setTesterAuth(String username) {
        var auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_TESTER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void setAdminAuth(String username) {
        var auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // -------------------------
    // createIssue tests
    // -------------------------

    @Test
    void createIssue_success() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug title",
                "Bug description",
                "HIGH"
        );

        assertNotNull(issue.getId());
        assertEquals(IssueStatus.OPEN, issue.getStatus());
        assertEquals(IssuePriority.HIGH, issue.getPriority());
    }

    @Test
    void createIssue_invalidPriority() {

        assertThrows(IllegalArgumentException.class, () ->
                issueService.createIssue(
                        project.getId(),
                        tester.getId(),
                        "Bug",
                        "desc",
                        "CRITICAL"
                )
        );
    }

    @Test
    void createIssue_archivedProject() {

        project.setStatus("ARCHIVED");
        projectRepository.save(project);

        assertThrows(IllegalStateException.class, () ->
                issueService.createIssue(
                        project.getId(),
                        tester.getId(),
                        "Bug",
                        "desc",
                        "LOW"
                )
        );
    }

    // -------------------------
    // assignIssue tests
    // -------------------------

    @Test
    void assignIssue_success() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "MEDIUM"
        );

        Issue assigned = issueService.assignIssue(issue.getId(), developer.getId());

        assertEquals(developer.getId(), assigned.getAssignedTo().getId());
    }

    @Test
    void assignIssue_sameDeveloperTwice() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "MEDIUM"
        );

        issueService.assignIssue(issue.getId(), developer.getId());

        assertThrows(IllegalArgumentException.class, () ->
                issueService.assignIssue(issue.getId(), developer.getId())
        );
    }

    @Test
    void assignIssue_nonDeveloper() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "LOW"
        );

        assertThrows(IllegalArgumentException.class, () ->
                issueService.assignIssue(issue.getId(), tester.getId())
        );
    }

    // -------------------------
    // filterIssues tests
    // -------------------------

    @Test
    void filterIssues_byProject() {

        issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug1",
                "desc",
                "HIGH"
        );

        List<Issue> issues = issueService.filterIssues(
                project.getId(),
                null,
                null
        );

        assertEquals(1, issues.size());
    }

    @Test
    void filterIssues_byPriority() {

        issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "HIGH"
        );

        List<Issue> issues = issueService.filterIssues(
                null,
                null,
                "HIGH"
        );

        assertEquals(1, issues.size());
        assertEquals(IssuePriority.HIGH, issues.get(0).getPriority());
    }

    @Test
    void filterIssues_invalidStatus() {

        assertThrows(IllegalArgumentException.class, () ->
                issueService.filterIssues(null, "DONE", null)
        );
    }

    @Test
    void filterIssues_invalidPriority() {

        assertThrows(IllegalArgumentException.class, () ->
                issueService.filterIssues(null, null, "URGENT")
        );
    }

    // -------------------------
    // updateStatus workflow tests
    // -------------------------

    @Test
    void developerCanStartIssue() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "HIGH"
        );

        issueService.assignIssue(issue.getId(), developer.getId());

        setDeveloperAuth(developer.getUsername());

        Issue updated = issueService.updateStatus(
                issue.getId(),
                "IN_PROGRESS",
                null
        );

        assertEquals(IssueStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void resolveRequiresResolutionNote() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "HIGH"
        );

        issueService.assignIssue(issue.getId(), developer.getId());

        setDeveloperAuth(developer.getUsername());

        issueService.updateStatus(issue.getId(), "IN_PROGRESS", null);

        assertThrows(IllegalArgumentException.class, () ->
                issueService.updateStatus(issue.getId(), "RESOLVED", null)
        );
    }

    @Test
    void testerCanVerifyIssue() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "HIGH"
        );

        issueService.assignIssue(issue.getId(), developer.getId());

        setDeveloperAuth(developer.getUsername());
        issueService.updateStatus(issue.getId(), "IN_PROGRESS", null);
        issueService.updateStatus(issue.getId(), "RESOLVED", "Fixed bug");

        setTesterAuth(tester.getUsername());

        Issue verified = issueService.updateStatus(issue.getId(), "VERIFIED", null);

        assertEquals(IssueStatus.VERIFIED, verified.getStatus());
    }

    @Test
    void onlyAdminCanCloseIssue() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "HIGH"
        );

        issueService.assignIssue(issue.getId(), developer.getId());

        setDeveloperAuth(developer.getUsername());
        issueService.updateStatus(issue.getId(), "IN_PROGRESS", null);
        issueService.updateStatus(issue.getId(), "RESOLVED", "Fixed");

        setTesterAuth(tester.getUsername());
        issueService.updateStatus(issue.getId(), "VERIFIED", null);

        setAdminAuth("admin");

        Issue closed = issueService.updateStatus(issue.getId(), "CLOSED", null);

        assertEquals(IssueStatus.CLOSED, closed.getStatus());
    }
}