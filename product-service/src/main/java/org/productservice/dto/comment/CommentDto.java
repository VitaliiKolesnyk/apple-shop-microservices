package org.productservice.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.productservice.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private String id;

    private String author;

    private String text;

    private LocalDateTime createdAt;

    private List<Comment> replies;
}
