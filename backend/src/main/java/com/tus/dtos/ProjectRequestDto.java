package com.tus.dtos;

public class ProjectRequestDto {

    private String name;
    private String description;

    public ProjectRequestDto() {
        // Needed for JSON
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}