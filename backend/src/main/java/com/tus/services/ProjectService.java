package com.tus.services;

import com.tus.db.models.Project;
import com.tus.db.repos.ProjectRepository;
import com.tus.db.models.Issue;
import com.tus.db.repos.IssueRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;

    // Constructor Injection
    public ProjectService(ProjectRepository projectRepository, IssueRepository issueRepository) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
    }

    // Create new project
    public Project createProject(String name, String description) {

        Optional<Project> existingProject = projectRepository.findByName(name);

        if (existingProject.isPresent()) {
            throw new IllegalArgumentException("Project with this name already exists");
        }

        Project project = new Project(name, description);

        return projectRepository.save(project);
    }

    // Get all projects
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // Archive project
    public Project archiveProject(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (project.getStatus().equals("ARCHIVED")) {
            throw new IllegalArgumentException("Project is already archived");
        }

        List<Issue> issues = issueRepository.findByProject(project);

        boolean allClosed = issues.stream()
                .allMatch(issue -> issue.getStatus() == com.tus.enums.IssueStatus.CLOSED);

        if (!allClosed) {
            throw new IllegalArgumentException("Project cannot be archived until all issues are CLOSED");
        }

        project.setStatus("ARCHIVED");

        return projectRepository.save(project);
    }

    // Reactivate project
    public Project reactivateProject(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (project.getStatus().equals("ACTIVE")) {
            throw new IllegalArgumentException("Project is already active");
        }

        project.setStatus("ACTIVE");

        return projectRepository.save(project);
    }
}