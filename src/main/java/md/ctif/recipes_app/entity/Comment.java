package md.ctif.recipes_app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.ctif.recipes_app.DTO.CommentDTO;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table("comments")
public class Comment {
    @Id
    private Long id;
    private String content;
    private Long recipeId;
    private Long parentCommentId;
    @CreatedDate
    private LocalDateTime createdAt;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @LastModifiedBy
    private String updatedBy;
    public Comment(CommentDTO comment) {
        this.content = comment.content();
        this.recipeId = comment.recipeId();
        this.parentCommentId = comment.parentCommentId();
    }
}
