package productservice.mapper.impl;

import org.productservice.dto.comment.CommentDto;
import org.productservice.entity.Comment;
import org.productservice.mapper.CommentMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentMapperImpl implements CommentMapper {
    @Override
    public CommentDto toCommentDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getAuthor(), comment.getText(),
                comment.getCreatedAt(), comment.getReplies());
    }

    @Override
    public Comment toEntity(CommentDto commentDto) {
        return new Comment(commentDto.getId(), commentDto.getAuthor(), commentDto.getText(),
                commentDto.getCreatedAt(), commentDto.getReplies(), "");
    }

    @Override
    public List<CommentDto> toCommentDtoList(List<Comment> comments) {
        return comments.stream().map(this::toCommentDto).toList();
    }
}
