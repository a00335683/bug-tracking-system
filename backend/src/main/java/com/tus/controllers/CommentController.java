package com.tus.controllers;

import com.tus.db.models.Comment;
import com.tus.services.CommentService;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public Comment addComment(
            @RequestParam Long issueId,
            @RequestParam Long userId,
            @RequestParam String message) {

        return commentService.addComment(issueId, userId, message);
    }

    @GetMapping("/issue/{issueId}")
    public List<Comment> getComments(@PathVariable Long issueId) {
        return commentService.getCommentsForIssue(issueId);
    }
}
