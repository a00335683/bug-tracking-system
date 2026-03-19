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
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    private Project project;
    private User tester;

    @BeforeEach
    void setup() {
        issueRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        project = new Project("Project A", "Test project");
        projectRepository.save(project);

        tester = new User("tester1", "pass", "TESTER", true);
        userRepository.save(tester);
    }

    @Test
    void createProject_success() {
        Project created = projectService.createProject("Project B", "New project");

        assertNotNull(created.getId());
        assertEquals("Project B", created.getName());
        assertEquals("ACTIVE", created.getStatus());
    }

    @Test
    void createProject_duplicateName() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.createProject("Project A", "Duplicate")
        );
    }

    @Test
    void archiveProject_successWhenAllIssuesClosed() {
        Issue issue = new Issue("Bug 1", "desc", IssuePriority.HIGH, project, tester);
        issue.setStatus(IssueStatus.CLOSED);
        issueRepository.save(issue);

        Project archived = projectService.archiveProject(project.getId());

        assertEquals("ARCHIVED", archived.getStatus());
    }

    @Test
    void archiveProject_failsWhenAnyIssueNotClosed() {
        Issue issue = new Issue("Bug 1", "desc", IssuePriority.MEDIUM, project, tester);
        issue.setStatus(IssueStatus.OPEN);
        issueRepository.save(issue);

        Long projectId = project.getId();

        assertThrows(IllegalArgumentException.class, () ->
                projectService.archiveProject(projectId)
        );
    }

    @Test
    void archiveProject_alreadyArchived() {
        project.setStatus("ARCHIVED");
        projectRepository.save(project);

        Long projectId = project.getId();

        assertThrows(IllegalArgumentException.class, () ->
                projectService.archiveProject(projectId)
        );
    }

    @Test
    void reactivateProject_success() {
        project.setStatus("ARCHIVED");
        projectRepository.save(project);

        Project reactivated = projectService.reactivateProject(project.getId());

        assertEquals("ACTIVE", reactivated.getStatus());
    }

    @Test
    void reactivateProject_alreadyActive() {
        Long projectId = project.getId();

        assertThrows(IllegalArgumentException.class, () ->
                projectService.reactivateProject(projectId)
        );
    }

    @Test
    void reactivateProject_projectNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.reactivateProject(99999L)
        );
    }
}