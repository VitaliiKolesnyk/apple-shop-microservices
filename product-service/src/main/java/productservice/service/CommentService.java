package productservice.service;

import org.productservice.dto.comment.CommentDto;
import org.productservice.dto.comment.CommentRequest;

import java.util.List;

public interface CommentService {

    CommentDto addComment(String productId, CommentRequest commentRequest);

    CommentDto addReply(String productId, String commentId, CommentRequest commentRequest);

    List<CommentDto> getAllCommentsByProductId(String productId);

    void deleteComment(String productId, String commentId);
}
