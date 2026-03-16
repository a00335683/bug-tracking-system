package com.tus.dtos;

public class UpdateIssueStatusRequestDto {

    private String status;
    private String resolutionNote;

    public UpdateIssueStatusRequestDto() {
        // Needed for JSON
    }

    public String getStatus() {
        return status;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }
}