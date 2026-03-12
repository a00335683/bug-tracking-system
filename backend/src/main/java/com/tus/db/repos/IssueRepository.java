package com.tus.db.repos;

import com.tus.db.models.Issue;
import com.tus.db.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    // Get all issues for a specific project
    List<Issue> findByProject(Project project);

    // Alternative lookup by project ID
    List<Issue> findByProjectId(Long projectId);
}