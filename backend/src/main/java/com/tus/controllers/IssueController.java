package com.tus.controllers;

import com.tus.db.models.Issue;
import com.tus.dtos.AssignIssueRequestDto;
import com.tus.dtos.IssueRequestDto;
import com.tus.dtos.IssueResponseDto;
import com.tus.dtos.UpdateIssueStatusRequestDto;
import com.tus.services.IssueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    // Create issue using JSON body
    @PostMapping
    public ResponseEntity<IssueResponseDto> createIssue(@RequestBody IssueRequestDto requestDto) {

        Issue issue = issueService.createIssue(
                requestDto.getProjectId(),
                requestDto.getReporterId(),
                requestDto.getTitle(),
                requestDto.getDescription(),
                requestDto.getPriority()
        );

        IssueResponseDto responseDto = new IssueResponseDto(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus().name(),
                issue.getPriority().name(),
                issue.getCreatedAt(),
                issue.getProject().getId(),
                issue.getReportedBy().getId(),
                issue.getAssignedTo() == null ? null : issue.getAssignedTo().getId()
        );

        return ResponseEntity.ok(responseDto);
    }

    // Get all issues
    @GetMapping
    public ResponseEntity<List<IssueResponseDto>> getAllIssues() {

        List<IssueResponseDto> responseList = issueService.getAllIssues()
                .stream()
                .map(issue -> new IssueResponseDto(
                        issue.getId(),
                        issue.getTitle(),
                        issue.getDescription(),
                        issue.getStatus().name(),
                        issue.getPriority().name(),
                        issue.getCreatedAt(),
                        issue.getProject().getId(),
                        issue.getReportedBy().getId(),
                        issue.getAssignedTo() == null ? null : issue.getAssignedTo().getId()
                ))
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<IssueResponseDto> assignIssue(
            @PathVariable Long id,
            @RequestBody AssignIssueRequestDto requestDto) {

        Issue issue = issueService.assignIssue(id, requestDto.getDeveloperId());

        IssueResponseDto responseDto = new IssueResponseDto(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus().name(),
                issue.getPriority().name(),
                issue.getCreatedAt(),
                issue.getProject().getId(),
                issue.getReportedBy().getId(),
                issue.getAssignedTo() == null ? null : issue.getAssignedTo().getId()
        );

        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<IssueResponseDto> updateIssueStatus(
            @PathVariable Long id,
            @RequestBody UpdateIssueStatusRequestDto requestDto) {

        Issue issue = issueService.updateStatus(id, requestDto.getStatus());

        IssueResponseDto responseDto = new IssueResponseDto(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus().name(),
                issue.getPriority().name(),
                issue.getCreatedAt(),
                issue.getProject().getId(),
                issue.getReportedBy().getId(),
                issue.getAssignedTo() == null ? null : issue.getAssignedTo().getId()
        );

        return ResponseEntity.ok(responseDto);
    }
}