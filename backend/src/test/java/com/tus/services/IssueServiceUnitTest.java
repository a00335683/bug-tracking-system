package com.tus.services;

import com.tus.db.models.Issue;
import com.tus.db.models.Project;
import com.tus.db.repos.IssueRepository;
import com.tus.db.repos.ProjectRepository;
import com.tus.db.repos.UserRepository;
import com.tus.enums.IssueStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceUnitTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IssueService issueService;

    @Test
    void createIssue_projectNotFound() {

        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                issueService.createIssue(
                        1L,
                        2L,
                        "Bug",
                        "Description",
                        "HIGH"
                )
        );
    }

    @Test
    void createIssue_reporterNotFound() {

        Project project = new Project("Test", "Desc");
        project.setId(1L);
        project.setStatus("ACTIVE");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                issueService.createIssue(
                        1L,
                        2L,
                        "Bug",
                        "Description",
                        "HIGH"
                )
        );
    }

    @Test
    void assignIssue_closedIssueNotAllowed() {

        Issue issue = new Issue();
        issue.setId(1L);
        issue.setStatus(IssueStatus.CLOSED);

        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));

        assertThrows(IllegalArgumentException.class, () ->
                issueService.assignIssue(1L, 2L)
        );
    }

    @Test
    void assignIssue_developerNotFound() {

        Issue issue = new Issue();
        issue.setId(1L);
        issue.setStatus(IssueStatus.OPEN);

        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                issueService.assignIssue(1L, 2L)
        );
    }
}