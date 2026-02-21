package com.tus.controllers;

import com.tus.db.models.Project;
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
    public ResponseEntity<Project> createProject(@RequestParam String name,
                                                 @RequestParam String description) {

        Project project = projectService.createProject(name, description);

        return ResponseEntity.ok(project);
    }

    // Get all projects
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {

        List<Project> projects = projectService.getAllProjects();

        return ResponseEntity.ok(projects);
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
}