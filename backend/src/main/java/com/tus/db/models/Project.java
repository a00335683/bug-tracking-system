package com.tus.db.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    // Project status: ACTIVE or ARCHIVED
    @Column(nullable = false)
    private String status; // ACTIVE or ARCHIVED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Project() {
        this.createdAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}