package com.tus.controllers;

import com.tus.db.models.Project;
import com.tus.dtos.ProjectRequestDto;
import com.tus.dtos.ProjectResponseDto;
import com.tus.services.ProjectService;
import org.springframework.http.ResponseEntity;
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
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @RequestBody ProjectRequestDto requestDto) {

        Project project = projectService.createProject(
                requestDto.getName(),
                requestDto.getDescription()
        );

        ProjectResponseDto responseDto = new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt()
        );

        return ResponseEntity.ok(responseDto);
    }

    // Get all projects
    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {

        List<Project> projects = projectService.getAllProjects();

        List<ProjectResponseDto> responseList = projects.stream()
                .map(project -> new ProjectResponseDto(
                        project.getId(),
                        project.getName(),
                        project.getDescription(),
                        project.getStatus(),
                        project.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<ProjectResponseDto> archiveProject(@PathVariable Long id) {

        Project project = projectService.archiveProject(id);

        ProjectResponseDto responseDto = new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt()
        );

        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{projectId}/reactivate")
    public ResponseEntity<ProjectResponseDto> reactivateProject(@PathVariable Long projectId) {

        Project project = projectService.reactivateProject(projectId);

        ProjectResponseDto responseDto = new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt()
        );

        return ResponseEntity.ok(responseDto);
    }
}