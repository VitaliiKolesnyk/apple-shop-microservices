package org.productservice.controller;

import lombok.RequiredArgsConstructor;
import org.productservice.dto.comment.CommentDto;
import org.productservice.dto.comment.CommentRequest;
import org.productservice.repository.CommentRepository;
import org.productservice.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentRepository commentRepository;

    @PostMapping("{productId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable String productId, @RequestBody CommentRequest commentRequest) {
        return commentService.addComment(productId, commentRequest);
    }

    @PostMapping("{productId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable String productId, @PathVariable String commentId, @RequestBody CommentRequest commentRequest) {
        return commentService.addReply(productId, commentId, commentRequest);
    }

    @GetMapping("{productId}/comments")
    public List<CommentDto> getComments(@PathVariable String productId) {
        return commentService.getAllCommentsByProductId(productId);
    }

    @DeleteMapping("{productId}/comments/{commentId}")
    public void deleteComment(@PathVariable String productId, @PathVariable String commentId) {
        commentService.deleteComment(productId, commentId);
    }
}
