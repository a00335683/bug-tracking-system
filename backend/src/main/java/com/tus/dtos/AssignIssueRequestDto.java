package com.tus.dtos;

public class AssignIssueRequestDto {

    private Long developerId;

    public AssignIssueRequestDto() {
        // Needed for JSON
    }

    public Long getDeveloperId() {
        return developerId;
    }
}