package productservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.productservice.dto.comment.CommentDto;
import org.productservice.dto.comment.CommentRequest;
import org.productservice.entity.Comment;
import org.productservice.entity.Product;
import org.productservice.mapper.CommentMapper;
import org.productservice.repository.CommentRepository;
import org.productservice.repository.ProductRepository;
import org.productservice.service.CommentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final ProductRepository productRepository;

    private final CommentMapper commentMapper;

    @Override
    public CommentDto addComment(String productId, CommentRequest commentRequest) {
        Comment comment = new Comment();
        comment.setText(commentRequest.getText());
        comment.setAuthor(commentRequest.getAuthor());
        comment.setProductId(productId);
        comment.setCreatedAt(LocalDateTime.now());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Comment result = commentRepository.save(comment);

        product.getComments().add(comment);

        productRepository.save(product);

        return commentMapper.toCommentDto(result);
    }

    @Override
    public CommentDto addReply(String productId, String commentId, CommentRequest commentRequest) {
        List<Comment> comments = commentRepository.findAllByProductId(productId);

        Comment topParentComment = findTopParentComment(comments, commentId);

        CommentDto commentDto = new CommentDto();
        commentDto.setText(commentRequest.getText());
        commentDto.setAuthor(commentRequest.getAuthor());
        commentDto.setCreatedAt(LocalDateTime.now());
        commentDto.setId(UUID.randomUUID().toString());

        addReplyToParentComment(topParentComment, commentMapper.toEntity(commentDto), commentId);

        commentRepository.save(topParentComment);

        return commentDto;
    }

    private Comment findTopParentComment(List<Comment> comments, String commentId) {
        for (Comment comment : comments) {

            if (comment.getId().equals(commentId)) {
                return comment;
            }

            if (!comment.getReplies().isEmpty()) {
                Comment found = findCommentInTree(comment.getReplies(), commentId);
                if (found != null) {
                    return comment;
                }
            }
        }
        return null;
    }

    private Comment findCommentInTree(List<Comment> comments, String commentId) {
        for (Comment comment : comments) {
            if (comment.getId().equals(commentId)) {
                return comment;
            }

            if (!comment.getReplies().isEmpty()) {
                Comment found = findCommentInTree(comment.getReplies(), commentId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private boolean addReplyToParentComment(Comment topParentComment, Comment reply, String commentId) {
        if (topParentComment.getId().equals(commentId)) {
            topParentComment.getReplies().add(reply);
            return true;
        }

        for (Comment replyComment : topParentComment.getReplies()) {
            if (replyComment.getId().equals(commentId)) {
                replyComment.getReplies().add(reply);

                return true;
            }
        }


        for (Comment replyComment : topParentComment.getReplies()) {
            boolean isAdded = addReplyToParentComment(replyComment, replyComment, commentId);
            if (isAdded) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<CommentDto> getAllCommentsByProductId(String productId) {
        return commentMapper.toCommentDtoList(commentRepository.findAllByProductId(productId));
    }

    @Override
    public void deleteComment(String productId, String commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);

        if (comment != null) {
            commentRepository.delete(comment);
        } else {
            List<Comment> comments = commentRepository.findAllByProductId(productId);

            Comment topParentComment = findTopParentComment(comments, commentId);

            boolean removed = deleteReply(topParentComment, commentId);

            if (removed) {
                commentRepository.save(topParentComment);
            }
        }
    }

    private boolean deleteReply(Comment parentComment, String replyId) {
        for (Comment reply : parentComment.getReplies()) {
            if (reply.getId().equals(replyId)) {
                parentComment.getReplies().remove(reply);
                return true;
            }
        }

        for (Comment reply : parentComment.getReplies()) {
            if (deleteReply(reply, replyId)) {
                return true;
            }
        }

        return false;
    }
}
