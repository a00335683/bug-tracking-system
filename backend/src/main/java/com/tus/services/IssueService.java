package com.tus.services;

import com.tus.db.models.Issue;
import com.tus.db.models.Project;
import com.tus.db.models.User;
import com.tus.db.repos.IssueRepository;
import com.tus.db.repos.ProjectRepository;
import com.tus.db.repos.UserRepository;
import org.springframework.stereotype.Service;

import com.tus.enums.IssuePriority;
import com.tus.enums.IssueStatus;

import java.util.List;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public IssueService(IssueRepository issueRepository,
                        ProjectRepository projectRepository,
                        UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    // Create new issue
    public Issue createIssue(Long projectId,
                             Long reporterId,
                             String title,
                             String description,
                             String priority) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("Cannot create issue for archived project");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found"));

        IssuePriority issuePriority = IssuePriority.valueOf(priority.toUpperCase());
        Issue issue = new Issue(title, description, issuePriority, project, reporter);

        return issueRepository.save(issue);
    }

    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    public Issue assignIssue(Long issueId, Long developerId) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        User developer = userRepository.findById(developerId)
                .orElseThrow(() -> new RuntimeException("Developer not found"));

        // Simple role check (we'll improve later)
        if (!"DEVELOPER".equals(developer.getRole())) {
            throw new RuntimeException("Selected user is not a developer");
        }

        issue.setAssignedTo(developer);
        issue.setStatus(IssueStatus.IN_PROGRESS);

        return issueRepository.save(issue);
    }

    public Issue updateStatus(Long issueId, String newStatus) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        IssueStatus targetStatus = IssueStatus.valueOf(newStatus.toUpperCase());
        IssueStatus currentStatus = issue.getStatus();

        // Valid transitions only
        boolean allowed =
                (currentStatus == IssueStatus.OPEN && targetStatus == IssueStatus.IN_PROGRESS) ||
                        (currentStatus == IssueStatus.IN_PROGRESS && targetStatus == IssueStatus.RESOLVED) ||
                        (currentStatus == IssueStatus.RESOLVED && targetStatus == IssueStatus.VERIFIED) ||
                        (currentStatus == IssueStatus.VERIFIED && targetStatus == IssueStatus.CLOSED);

        if (!allowed) {
            throw new RuntimeException("Invalid status transition: " + currentStatus + " -> " + targetStatus);
        }

        issue.setStatus(targetStatus);
        return issueRepository.save(issue);
    }
}