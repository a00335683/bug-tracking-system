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

        Long projectId = project.getId();
        Long testerId = tester.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.createIssue(
                        projectId,
                        testerId,
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

        Long projectId = project.getId();
        Long testerId = tester.getId();

        assertThrows(IllegalStateException.class, () ->
                issueService.createIssue(
                        projectId,
                        testerId,
                        "Bug",
                        "desc",
                        "LOW"
                )
        );
    }

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

        Long issueId = issue.getId();
        Long developerId = developer.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.assignIssue(issueId, developerId)
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

        Long issueId = issue.getId();
        Long testerId = tester.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.assignIssue(issueId, testerId)
        );
    }

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

        Long issueId = issue.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.updateStatus(issueId, "RESOLVED", null)
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

    @Test
    void createIssue_titleRequired() {

        Long projectId = project.getId();
        Long testerId = tester.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.createIssue(
                        projectId,
                        testerId,
                        "",
                        "Bug description",
                        "HIGH"
                )
        );
    }

    @Test
    void createIssue_descriptionRequired() {

        Long projectId = project.getId();
        Long testerId = tester.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.createIssue(
                        projectId,
                        testerId,
                        "Bug title",
                        "",
                        "HIGH"
                )
        );
    }

    @Test
    void createIssue_projectNotFound() {

        Long testerId = tester.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.createIssue(
                        99999L,
                        testerId,
                        "Bug",
                        "desc",
                        "HIGH"
                )
        );
    }

    @Test
    void createIssue_reporterNotFound() {

        Long projectId = project.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.createIssue(
                        projectId,
                        99999L,
                        "Bug",
                        "desc",
                        "HIGH"
                )
        );
    }

    @Test
    void getAllIssues_returnsAllIssues() {

        issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug1",
                "desc1",
                "HIGH"
        );

        issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug2",
                "desc2",
                "LOW"
        );

        List<Issue> issues = issueService.getAllIssues();

        assertEquals(2, issues.size());
    }

    @Test
    void assignIssue_closedIssue() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "MEDIUM"
        );

        issue.setStatus(IssueStatus.CLOSED);
        issueRepository.save(issue);

        Long issueId = issue.getId();
        Long developerId = developer.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.assignIssue(issueId, developerId)
        );
    }

    @Test
    void assignIssue_issueNotFound() {

        Long developerId = developer.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.assignIssue(99999L, developerId)
        );
    }

    @Test
    void assignIssue_developerNotFound() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "MEDIUM"
        );

        Long issueId = issue.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.assignIssue(issueId, 99999L)
        );
    }

    @Test
    void updateStatus_issueNotFound() {

        setDeveloperAuth(developer.getUsername());

        assertThrows(IllegalArgumentException.class, () ->
                issueService.updateStatus(99999L, "IN_PROGRESS", null)
        );
    }

    @Test
    void updateStatus_closedIssueCannotBeModified() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "HIGH"
        );

        issue.setStatus(IssueStatus.CLOSED);
        issueRepository.save(issue);

        setAdminAuth("admin");

        Long issueId = issue.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.updateStatus(issueId, "CLOSED", null)
        );
    }

    @Test
    void updateStatus_invalidStatusValue() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "HIGH"
        );

        issueService.assignIssue(issue.getId(), developer.getId());
        setDeveloperAuth(developer.getUsername());

        Long issueId = issue.getId();

        assertThrows(IllegalArgumentException.class, () ->
                issueService.updateStatus(issueId, "DONE", null)
        );
    }

    @Test
    void updateStatus_invalidTransition() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "HIGH"
        );

        issueService.assignIssue(issue.getId(), developer.getId());
        setDeveloperAuth(developer.getUsername());

        Long issueId = issue.getId();

        assertThrows(IllegalStateException.class, () ->
                issueService.updateStatus(issueId, "VERIFIED", null)
        );
    }

    @Test
    void testerCannotMoveIssueToInProgress() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "HIGH"
        );

        issueService.assignIssue(issue.getId(), developer.getId());
        setTesterAuth(tester.getUsername());

        Long issueId = issue.getId();

        assertThrows(IllegalStateException.class, () ->
                issueService.updateStatus(issueId, "IN_PROGRESS", null)
        );
    }

    @Test
    void developerCannotVerifyIssue() {

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

        Long issueId = issue.getId();

        assertThrows(IllegalStateException.class, () ->
                issueService.updateStatus(issueId, "VERIFIED", null)
        );
    }

    @Test
    void testerCannotCloseIssue() {

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

        Long issueId = issue.getId();

        assertThrows(IllegalStateException.class, () ->
                issueService.updateStatus(issueId, "CLOSED", null)
        );
    }

    @Test
    void developerCannotUpdateIssueAssignedToAnotherDeveloper() {

        User anotherDeveloper = new User("dev2", "pass", "DEVELOPER", true);
        userRepository.save(anotherDeveloper);

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "HIGH"
        );

        issueService.assignIssue(issue.getId(), anotherDeveloper.getId());

        setDeveloperAuth(developer.getUsername());

        Long issueId = issue.getId();

        assertThrows(IllegalStateException.class, () ->
                issueService.updateStatus(issueId, "IN_PROGRESS", null)
        );
    }

    @Test
    void developerCannotUpdateUnassignedIssue() {

        Issue issue = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug",
                "desc",
                "HIGH"
        );

        setDeveloperAuth(developer.getUsername());

        Long issueId = issue.getId();

        assertThrows(IllegalStateException.class, () ->
                issueService.updateStatus(issueId, "IN_PROGRESS", null)
        );
    }

    @Test
    void filterIssues_byStatus() {

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

        List<Issue> issues = issueService.filterIssues(
                null,
                "IN_PROGRESS",
                null
        );

        assertEquals(1, issues.size());
        assertEquals(IssueStatus.IN_PROGRESS, issues.get(0).getStatus());
    }

    @Test
    void filterIssues_byStatusAndPriority() {

        Issue issue1 = issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug1",
                "desc1",
                "HIGH"
        );

        issueService.assignIssue(issue1.getId(), developer.getId());
        setDeveloperAuth(developer.getUsername());
        issueService.updateStatus(issue1.getId(), "IN_PROGRESS", null);

        issueService.createIssue(
                project.getId(),
                tester.getId(),
                "Bug2",
                "desc2",
                "LOW"
        );

        List<Issue> issues = issueService.filterIssues(
                null,
                "IN_PROGRESS",
                "HIGH"
        );

        assertEquals(1, issues.size());
        assertEquals(IssueStatus.IN_PROGRESS, issues.get(0).getStatus());
        assertEquals(IssuePriority.HIGH, issues.get(0).getPriority());
    }
}