package com.tus.db.models;

import com.tus.enums.IssuePriority;
import com.tus.enums.IssueStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;// OPEN, IN_PROGRESS, RESOLVED, VERIFIED, CLOSED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuePriority priority; // LOW, MEDIUM, HIGH

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Many issues belong to one project
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Reporter
    @ManyToOne
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    // Assigned developer (can be null initially)
    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    public Issue() {
        this.createdAt = LocalDateTime.now();
        this.status = IssueStatus.OPEN;
    }

    public Issue(String title, String description, IssuePriority priority,
                 Project project, User reportedBy) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.project = project;
        this.reportedBy = reportedBy;
        this.status = IssueStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public IssueStatus getStatus() { return status; }

    public void setStatus(IssueStatus status) { this.status = status; }

    public IssuePriority getPriority() { return priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Project getProject() { return project; }

    public User getReportedBy() { return reportedBy; }

    public User getAssignedTo() { return assignedTo; }

    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
}