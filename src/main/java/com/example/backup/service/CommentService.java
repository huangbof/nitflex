package com.example.backup.service;

import com.example.backup.model.Comment;
import com.example.backup.repository.CommentRepository;

import java.util.List;

public class CommentService {

    private final CommentRepository commentRepo = new CommentRepository();

    public List<Comment> getCommentsByMovie(int movieId) {
        return commentRepo.findByMovieId(movieId);
    }

    public boolean addComment(int userId, int movieId, String content) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setMovieId(movieId);
        comment.setContent(content);
        return commentRepo.save(comment);
    }

    public boolean deleteComment(int commentId, int userId) {
        return commentRepo.delete(commentId, userId);
    }
}
