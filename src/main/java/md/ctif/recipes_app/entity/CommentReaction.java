package md.ctif.recipes_app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table
public class CommentReaction {
    @Id
    private Long id;
    private Long commentId;
    private String reactionType;
    @CreatedDate
    private LocalDateTime createdAt;
    @CreatedBy
    private String createdBy;
}
