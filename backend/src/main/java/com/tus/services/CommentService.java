package com.tus.services;

import com.tus.db.models.Comment;
import com.tus.db.models.Issue;
import com.tus.db.models.User;
import com.tus.db.repos.CommentRepository;
import com.tus.db.repos.IssueRepository;
import com.tus.db.repos.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          IssueRepository issueRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Long issueId, Long userId, String message) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setUser(user);
        comment.setMessage(message);

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsForIssue(Long issueId) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        return commentRepository.findByIssueOrderByCreatedDateAsc(issue);
    }
}
