package md.ctif.recipes_app.repository;

import md.ctif.recipes_app.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {
    Flux<Comment> findAllByParentCommentIdAndRecipeId(Long parentCommentId, Long recipeId, Pageable pageable);

    Flux<Comment> findByParentCommentIdIsNullAndRecipeIdOrderByCreatedAtDesc(Long recipeId, Pageable pageable);

    Flux<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    Mono<Long> countByParentCommentId(Long parentCommentId);

    @Query("""
        WITH RECURSIVE comment_tree AS (
            SELECT id, content, recipe_id, parent_comment_id, created_at, created_by, updated_at, updated_by, 0 as level
            FROM comments 
            WHERE id = :commentId
            UNION ALL
            SELECT c.id, c.content, c.recipe_id, c.parent_comment_id, c.created_at, c.created_by, c.updated_at, c.updated_by, ct.level + 1
            FROM comments c
            INNER JOIN comment_tree ct ON c.parent_comment_id = ct.id
        )
        SELECT * FROM comment_tree ORDER BY level, created_at
        """)
    Flux<Comment> findCommentWithDescendants(@Param("commentId") Long commentId);
}
