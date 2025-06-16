package md.ctif.recipes_app.DTO;

import md.ctif.recipes_app.entity.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CommentDTO(
        Long id,
        String keycloakId,
        Long recipeId,
        Long parentCommentId,
        LocalDateTime createdAt,
        String content,
        List<CommentDTO> children,
        Boolean hasChildren) {
    public static CommentDTO commentDTOFromComment(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getCreatedBy(),
                comment.getRecipeId(),
                comment.getParentCommentId(),
                comment.getCreatedAt(),
                comment.getContent(),
                new ArrayList<>(),
                false
        );
    }
    public static CommentDTO withList(CommentDTO commentDTO, List<CommentDTO> comments) {
        return new CommentDTO(
                commentDTO.id(),
                commentDTO.keycloakId,
                commentDTO.recipeId,
                commentDTO.parentCommentId,
                commentDTO.createdAt,
                commentDTO.content,
                comments,
                !comments.isEmpty()
        );
    }

    public static CommentDTO hasChildren(CommentDTO commentDTO, Boolean hasChildren) {
        return new CommentDTO(
                commentDTO.id(),
                commentDTO.keycloakId,
                commentDTO.recipeId,
                commentDTO.parentCommentId,
                commentDTO.createdAt,
                commentDTO.content,
                new ArrayList<>(),
                hasChildren
        );
    }
}
