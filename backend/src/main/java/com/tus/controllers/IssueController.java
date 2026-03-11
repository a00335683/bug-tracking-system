package com.tus.controllers;

import com.tus.db.models.Issue;
import com.tus.dtos.AssignIssueRequestDto;
import com.tus.dtos.IssueRequestDto;
import com.tus.dtos.IssueResponseDto;
import com.tus.dtos.UpdateIssueStatusRequestDto;
import com.tus.services.IssueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('TESTER')")
    @PostMapping
    public ResponseEntity<IssueResponseDto> createIssue(@RequestBody IssueRequestDto requestDto) {

        Issue issue = issueService.createIssue(
                requestDto.getProjectId(),
                requestDto.getReporterId(),
                requestDto.getTitle(),
                requestDto.getDescription(),
                requestDto.getPriority()
        );

        return ResponseEntity.status(201).body(toDto(issue));
    }

    // Get all issues
    @GetMapping
    public ResponseEntity<List<IssueResponseDto>> getAllIssues() {

        List<IssueResponseDto> responseList = issueService.getAllIssues()
                .stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/assign")
    public ResponseEntity<IssueResponseDto> assignIssue(
            @PathVariable Long id,
            @RequestBody AssignIssueRequestDto requestDto) {

        Issue issue = issueService.assignIssue(id, requestDto.getDeveloperId());

        return ResponseEntity.ok(toDto(issue));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<IssueResponseDto> updateIssueStatus(
            @PathVariable Long id,
            @RequestBody UpdateIssueStatusRequestDto requestDto) {

        Issue issue = issueService.updateStatus(
                id,
                requestDto.getStatus(),
                requestDto.getResolutionNote()
        );

        return ResponseEntity.ok(toDto(issue));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<IssueResponseDto>> filterIssues(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {

        List<IssueResponseDto> responseList = issueService
                .filterIssues(projectId, status, priority)
                .stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    private IssueResponseDto toDto(Issue issue) {
        return new IssueResponseDto(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus().name(),
                issue.getPriority().name(),
                issue.getCreatedAt(),
                issue.getProject() == null ? null : issue.getProject().getId(),
                issue.getReportedBy() == null ? null : issue.getReportedBy().getId(),
                issue.getAssignedTo() == null ? null : issue.getAssignedTo().getId(),
                issue.getResolutionNote(),
                issue.getAssignedTo() == null ? null : issue.getAssignedTo().getUsername()
        );
    }
}