package com.tus.dtos;

public class UpdateIssueStatusRequestDto {

    private String status;
    private Long userId;

    public UpdateIssueStatusRequestDto() {}

    public String getStatus() {
        return status;
    }

    public Long getUserId() {
        return userId;
    }
}