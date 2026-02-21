package com.tus.dtos;

import java.time.LocalDateTime;

public class ProjectResponseDto {

    private Long id;
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    public ProjectResponseDto(Long id, String name, String description,
                              String status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}