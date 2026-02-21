package com.tus.services;

import com.tus.db.models.Project;
import com.tus.db.repos.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    // Constructor Injection (as taught)
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // Create new project
    public Project createProject(String name, String description) {

        Optional<Project> existingProject = projectRepository.findByName(name);

        if (existingProject.isPresent()) {
            throw new RuntimeException("Project with this name already exists");
        }

        Project project = new Project(name, description);

        return projectRepository.save(project);
    }

    // Get all projects
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project archiveProject(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.getStatus().equals("ARCHIVED")) {
            throw new RuntimeException("Project is already archived");
        }

        project.setStatus("ARCHIVED");

        return projectRepository.save(project);
    }


}