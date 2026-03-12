package com.tus.services;

import com.tus.db.models.Issue;
import com.tus.db.models.Project;
import com.tus.db.models.User;
import com.tus.db.repos.IssueRepository;
import com.tus.db.repos.ProjectRepository;
import com.tus.db.repos.UserRepository;
import com.tus.enums.IssuePriority;
import com.tus.enums.IssueStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    // Create a new issue under an active project
    public Issue createIssue(Long projectId,
                             Long reporterId,
                             String title,
                             String description,
                             String priority) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!project.getStatus().equals("ACTIVE")) {
            throw new IllegalStateException("Cannot create issue for an archived project");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));

        IssuePriority issuePriority;
        try {
            issuePriority = IssuePriority.valueOf(priority.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid priority value");
        }

        Issue issue = new Issue(title, description, issuePriority, project, reporter);

        return issueRepository.save(issue);
    }

    // Get all issues
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    // Assign an issue to a developer
    public Issue assignIssue(Long issueId, Long developerId) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        if (issue.getStatus() == IssueStatus.CLOSED) {
            throw new IllegalArgumentException("Cannot assign a closed issue");
        }

        User developer = userRepository.findById(developerId)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found"));

        if (!"DEVELOPER".equals(developer.getRole())) {
            throw new IllegalArgumentException("Selected user is not a developer");
        }

        // Prevent assigning the same developer again
        if (issue.getAssignedTo() != null &&
                issue.getAssignedTo().getId().equals(developerId)) {
            throw new IllegalArgumentException("Issue is already assigned to this developer");
        }

        issue.setAssignedTo(developer);

        return issueRepository.save(issue);
    }

    // Update issue status based on role and workflow rules
    public Issue updateStatus(Long issueId, String newStatus, String resolutionNote) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        // Once an issue is closed, it should not be changed again
        if (issue.getStatus() == IssueStatus.CLOSED) {
            throw new IllegalArgumentException("Closed issues cannot be modified");
        }

        IssueStatus targetStatus;
        try {
            targetStatus = IssueStatus.valueOf(newStatus.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status value");
        }

        IssueStatus currentStatus = issue.getStatus();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isTester = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TESTER"));

        boolean isDeveloper = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER"));

        // Only allow the correct next step in the workflow
        boolean allowed =
                (currentStatus == IssueStatus.OPEN && targetStatus == IssueStatus.IN_PROGRESS) ||
                        (currentStatus == IssueStatus.IN_PROGRESS && targetStatus == IssueStatus.RESOLVED) ||
                        (currentStatus == IssueStatus.RESOLVED && targetStatus == IssueStatus.VERIFIED) ||
                        (currentStatus == IssueStatus.VERIFIED && targetStatus == IssueStatus.CLOSED);

        if (!allowed) {
            throw new IllegalStateException("Invalid status transition: " + currentStatus + " -> " + targetStatus);
        }

        // Only admin can close issues
        if (targetStatus == IssueStatus.CLOSED && !isAdmin) {
            throw new IllegalStateException("Only admin can close issues");
        }

        // Only tester can verify resolved issues
        if (targetStatus == IssueStatus.VERIFIED && !isTester) {
            throw new IllegalStateException("Only tester can verify issues");
        }

        // Only developer can move issue into progress or resolve it
        if ((targetStatus == IssueStatus.IN_PROGRESS || targetStatus == IssueStatus.RESOLVED) && !isDeveloper) {
            throw new IllegalStateException("Only developer can start or resolve issues");
        }

        // Developer should only work on issues assigned to them
        if (isDeveloper) {
            if (issue.getAssignedTo() == null ||
                    !issue.getAssignedTo().getUsername().equals(username)) {
                throw new IllegalStateException("You can only update issues assigned to you");
            }
        }

        // Resolution note is needed when resolving an issue
        if (targetStatus == IssueStatus.RESOLVED) {
            if (resolutionNote == null || resolutionNote.isBlank()) {
                throw new IllegalArgumentException("Resolution note is required when resolving an issue");
            }
            issue.setResolutionNote(resolutionNote);
        }

        issue.setStatus(targetStatus);

        return issueRepository.save(issue);
    }

    // Filter issues by project, status, and priority
    public List<Issue> filterIssues(Long projectId, String status, String priority) {

        List<Issue> issues = issueRepository.findAll();

        if (projectId != null) {
            issues = issues.stream()
                    .filter(issue -> issue.getProject().getId().equals(projectId))
                    .toList();
        }

        if (status != null && !status.isBlank()) {
            IssueStatus issueStatus;
            try {
                issueStatus = IssueStatus.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid status value");
            }

            issues = issues.stream()
                    .filter(issue -> issue.getStatus() == issueStatus)
                    .toList();
        }

        if (priority != null && !priority.isBlank()) {
            IssuePriority issuePriority;
            try {
                issuePriority = IssuePriority.valueOf(priority.toUpperCase());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid priority value");
            }

            issues = issues.stream()
                    .filter(issue -> issue.getPriority() == issuePriority)
                    .toList();
        }

        return issues;
    }
}