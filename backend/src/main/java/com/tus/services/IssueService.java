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

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_TESTER = "ROLE_TESTER";
    private static final String ROLE_DEVELOPER = "ROLE_DEVELOPER";

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

        IssuePriority issuePriority = parseIssuePriority(priority);

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

        validateIssueIsNotClosed(issue);

        IssueStatus targetStatus = parseIssueStatus(newStatus);
        IssueStatus currentStatus = issue.getStatus();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        validateTransition(currentStatus, targetStatus);
        validateRolePermissions(authentication, targetStatus);
        validateDeveloperAssignment(issue, authentication, username);
        applyResolutionNoteIfNeeded(issue, targetStatus, resolutionNote);

        issue.setStatus(targetStatus);
        return issueRepository.save(issue);
    }

    private void validateIssueIsNotClosed(Issue issue) {
        if (issue.getStatus() == IssueStatus.CLOSED) {
            throw new IllegalArgumentException("Closed issues cannot be modified");
        }
    }

    private void validateTransition(IssueStatus currentStatus, IssueStatus targetStatus) {
        if (!isAllowedTransition(currentStatus, targetStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition: " + currentStatus + " -> " + targetStatus
            );
        }
    }

    private void validateRolePermissions(Authentication authentication, IssueStatus targetStatus) {
        if (targetStatus == IssueStatus.CLOSED && !hasRole(authentication, ROLE_ADMIN)) {
            throw new IllegalStateException("Only admin can close issues");
        }

        if (targetStatus == IssueStatus.VERIFIED && !hasRole(authentication, ROLE_TESTER)) {
            throw new IllegalStateException("Only tester can verify issues");
        }

        if (requiresDeveloperRole(targetStatus) && !hasRole(authentication, ROLE_DEVELOPER)) {
            throw new IllegalStateException("Only developer can start or resolve issues");
        }
    }

    private void validateDeveloperAssignment(Issue issue, Authentication authentication, String username) {
        if (hasRole(authentication, ROLE_DEVELOPER) &&
                (issue.getAssignedTo() == null ||
                        !issue.getAssignedTo().getUsername().equals(username))) {
            throw new IllegalStateException("You can only update issues assigned to you");
        }
    }

    private void applyResolutionNoteIfNeeded(Issue issue, IssueStatus targetStatus, String resolutionNote) {
        if (targetStatus != IssueStatus.RESOLVED) {
            return;
        }

        if (resolutionNote == null || resolutionNote.isBlank()) {
            throw new IllegalArgumentException("Resolution note is required when resolving an issue");
        }

        issue.setResolutionNote(resolutionNote);
    }

    private boolean requiresDeveloperRole(IssueStatus targetStatus) {
        return targetStatus == IssueStatus.IN_PROGRESS || targetStatus == IssueStatus.RESOLVED;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
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
            IssueStatus issueStatus = parseIssueStatus(status);

            issues = issues.stream()
                    .filter(issue -> issue.getStatus() == issueStatus)
                    .toList();
        }

        if (priority != null && !priority.isBlank()) {
            IssuePriority issuePriority = parseIssuePriority(priority);

            issues = issues.stream()
                    .filter(issue -> issue.getPriority() == issuePriority)
                    .toList();
        }

        return issues;
    }

    // Delete an issue
    public void deleteIssue(Long issueId) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        if (issue.getStatus() != IssueStatus.OPEN) {
            throw new IllegalStateException("Only OPEN issues can be deleted");
        }

        issueRepository.delete(issue);
    }

    private IssueStatus parseIssueStatus(String status) {
        try {
            return IssueStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status value");
        }
    }

    private IssuePriority parseIssuePriority(String priority) {
        try {
            return IssuePriority.valueOf(priority.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid priority value");
        }
    }

    private boolean isAllowedTransition(IssueStatus currentStatus, IssueStatus targetStatus) {
        return (currentStatus == IssueStatus.OPEN && targetStatus == IssueStatus.IN_PROGRESS) ||
                (currentStatus == IssueStatus.IN_PROGRESS && targetStatus == IssueStatus.RESOLVED) ||
                (currentStatus == IssueStatus.RESOLVED && targetStatus == IssueStatus.VERIFIED) ||
                (currentStatus == IssueStatus.VERIFIED && targetStatus == IssueStatus.CLOSED);
    }
}