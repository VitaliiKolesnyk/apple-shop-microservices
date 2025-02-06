package productservice.mapper;

import org.productservice.dto.comment.CommentDto;
import org.productservice.entity.Comment;

import java.util.List;

public interface CommentMapper {

    CommentDto toCommentDto(Comment comment);

    Comment toEntity(CommentDto commentDto);

    List<CommentDto> toCommentDtoList(List<Comment> comments);
}
