package com.tus.db.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private String status; // OPEN, IN_PROGRESS, RESOLVED, VERIFIED, CLOSED

    @Column(nullable = false)
    private String priority; // LOW, MEDIUM, HIGH

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

    public Issue() {
        this.createdAt = LocalDateTime.now();
        this.status = "OPEN";
    }

    public Issue(String title, String description, String priority,
                 Project project, User reportedBy) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.project = project;
        this.reportedBy = reportedBy;
        this.status = "OPEN";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Project getProject() { return project; }

    public User getReportedBy() { return reportedBy; }

    public User getAssignedTo() { return assignedTo; }

    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
}