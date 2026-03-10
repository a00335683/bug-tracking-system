package com.tus.services;

import com.tus.db.models.Issue;
import com.tus.db.models.Project;
import com.tus.db.models.User;
import com.tus.db.repos.IssueRepository;
import com.tus.db.repos.ProjectRepository;
import com.tus.db.repos.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
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
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority value");
        }
        Issue issue = new Issue(title, description, issuePriority, project, reporter);

        return issueRepository.save(issue);
    }

    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

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

        issue.setAssignedTo(developer);

        return issueRepository.save(issue);
    }

    public Issue updateStatus(Long issueId, String newStatus, String resolutionNote) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        // closed issues cannot change
        if (issue.getStatus() == IssueStatus.CLOSED) {
            throw new IllegalArgumentException("Closed issues cannot be modified");
        }

        IssueStatus targetStatus = IssueStatus.valueOf(newStatus.toUpperCase());
        IssueStatus currentStatus = issue.getStatus();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isTester = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TESTER"));

        boolean isDeveloper = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER"));

        // Valid transitions only
        boolean allowed =
                (currentStatus == IssueStatus.OPEN && targetStatus == IssueStatus.IN_PROGRESS) ||
                        (currentStatus == IssueStatus.IN_PROGRESS && targetStatus == IssueStatus.RESOLVED) ||
                        (currentStatus == IssueStatus.RESOLVED && targetStatus == IssueStatus.VERIFIED) ||
                        (currentStatus == IssueStatus.VERIFIED && targetStatus == IssueStatus.CLOSED);

        if (targetStatus == IssueStatus.CLOSED && !isAdmin) {
            throw new IllegalStateException("Only admin can close issues");
        }

        if (targetStatus == IssueStatus.VERIFIED && !isTester) {
            throw new IllegalStateException("Only tester can verify issues");
        }

        if ((targetStatus == IssueStatus.IN_PROGRESS || targetStatus == IssueStatus.RESOLVED) && !isDeveloper) {
            throw new IllegalStateException("Only developer can start or resolve issues");
        }

        if (isDeveloper) {
            if (issue.getAssignedTo() == null ||
                    !issue.getAssignedTo().getUsername().equals(username)) {
                throw new IllegalStateException("You can only update issues assigned to you");
            }
        }

        if (!allowed) {
            throw new IllegalStateException("Invalid status transition: " + currentStatus + " -> " + targetStatus);
        }

        if (targetStatus == IssueStatus.RESOLVED && (resolutionNote == null || resolutionNote.isBlank())) {
            throw new IllegalArgumentException("Resolution note is required when resolving an issue");
        }

        if (targetStatus == IssueStatus.RESOLVED) {
            issue.setResolutionNote(resolutionNote);
        }

        issue.setStatus(targetStatus);
        return issueRepository.save(issue);
    }

    public List<Issue> filterIssues(Long projectId, String status, String priority) {

        List<Issue> issues = issueRepository.findAll();

        if (projectId != null) {
            issues = issues.stream()
                    .filter(i -> i.getProject().getId().equals(projectId))
                    .toList();
        }

        if (status != null && !status.isBlank()) {

            IssueStatus s;

            try {
                s = IssueStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value");
            }

            issues = issues.stream()
                    .filter(i -> i.getStatus() == s)
                    .toList();
        }

        if (priority != null && !priority.isBlank()) {

            IssuePriority p;

            try {
                p = IssuePriority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid priority value");
            }

            issues = issues.stream()
                    .filter(i -> i.getPriority() == p)
                    .toList();
        }

        return issues;
    }
}