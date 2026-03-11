package com.tus.controllers;

import com.tus.db.models.Project;
import com.tus.dtos.ProjectRequestDto;
import com.tus.dtos.ProjectResponseDto;
import com.tus.services.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    // Constructor Injection
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Create new project
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @Valid @RequestBody ProjectRequestDto requestDto) {

        Project project = projectService.createProject(
                requestDto.getName(),
                requestDto.getDescription()
        );

        return ResponseEntity.status(201).body(toDto(project));
    }

    // Get all projects
    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {

        List<ProjectResponseDto> responseList = projectService.getAllProjects()
                .stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/archive")
    public ResponseEntity<ProjectResponseDto> archiveProject(@PathVariable Long id) {

        Project project = projectService.archiveProject(id);

        return ResponseEntity.ok(toDto(project));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{projectId}/reactivate")
    public ResponseEntity<ProjectResponseDto> reactivateProject(@PathVariable Long projectId) {

        Project project = projectService.reactivateProject(projectId);

        return ResponseEntity.ok(toDto(project));
    }

    private ProjectResponseDto toDto(Project project) {
        return new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt()
        );
    }
}