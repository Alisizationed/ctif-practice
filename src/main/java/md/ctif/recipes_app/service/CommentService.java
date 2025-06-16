package md.ctif.recipes_app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import md.ctif.recipes_app.DTO.CommentDTO;
import md.ctif.recipes_app.entity.Comment;
import md.ctif.recipes_app.repository.CommentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Log4j2
public class CommentService {
    private final CommentRepository commentRepository;

    public Mono<ResponseEntity<String>> createComment(CommentDTO comment) {
        return commentRepository.save(new Comment(comment))
                .map(savedComment -> ResponseEntity.ok("Comment saved with ID: " + savedComment.getId()));
    }

    public Flux<CommentDTO> getCommentsPageable(Long recipeId, Pageable pageable) {
        return commentRepository.findAllByParentCommentIdAndRecipeId(null, recipeId, pageable)
                .map(CommentDTO::commentDTOFromComment);
    }

    // Fixed version - Option 1: Load full trees for paginated root comments
    public Flux<CommentDTO> getCommentsTreesPageable(Long recipeId, Pageable pageable) {
        return commentRepository.findByParentCommentIdIsNullAndRecipeIdOrderByCreatedAtDesc(recipeId, pageable)
                .flatMap(this::loadCommentTree);
    }

    // Helper method to recursively load comment tree
    private Mono<CommentDTO> loadCommentTree(Comment comment) {
        return commentRepository.findByParentCommentIdOrderByCreatedAtAsc(comment.getId())
                .flatMap(this::loadCommentTree) // Recursively load children
                .collectList()
                .map(children -> CommentDTO.withList(
                        CommentDTO.commentDTOFromComment(comment),children));
    }

    // Alternative Option 2: More efficient using recursive CTE query
    public Flux<CommentDTO> getCommentsTreesPageableEfficient(Long recipeId, Pageable pageable) {
        return commentRepository.findByParentCommentIdIsNullAndRecipeIdOrderByCreatedAtDesc(recipeId, pageable)
                .flatMap(rootComment -> {
                    // Use recursive CTE to get entire subtree in one query
                    return commentRepository.findCommentWithDescendants(rootComment.getId())
                            .collectList()
                            .map(allComments -> buildCommentTreeFromFlat(rootComment, allComments));
                });
    }

    // Helper method to build tree from flat list (from CTE query)
    private CommentDTO buildCommentTreeFromFlat(Comment rootComment, List<Comment> allComments) {
        Map<Long, CommentDTO> commentMap = new HashMap<>();
        Map<Long, List<CommentDTO>> childrenMap = new HashMap<>();

        // Convert all comments to DTOs and initialize children lists
        for (Comment comment : allComments) {
            CommentDTO dto = CommentDTO.commentDTOFromComment(comment);
            commentMap.put(comment.getId(), dto);
            childrenMap.put(comment.getId(), new ArrayList<>());
        }

        // Build parent-child relationships
        for (Comment comment : allComments) {
            if (comment.getParentCommentId() != null) {
                List<CommentDTO> siblings = childrenMap.get(comment.getParentCommentId());
                if (siblings != null) {
                    siblings.add(commentMap.get(comment.getId()));
                }
            }
        }

        // Set children for each comment
        for (Comment comment : allComments) {
            CommentDTO dto = commentMap.get(comment.getId());
            if (dto != null) {
                dto = CommentDTO.withList(dto,childrenMap.get(comment.getId()));
            }
        }

        return commentMap.get(rootComment.getId());
    }

    // Option 3: Lazy loading approach (load children on demand)
    public Flux<CommentDTO> getCommentsTreesPageableLazy(Long recipeId, Pageable pageable) {
        return commentRepository.findByParentCommentIdIsNullAndRecipeIdOrderByCreatedAtDesc(recipeId, pageable)
                .flatMap(comment -> {
                    CommentDTO dto = CommentDTO.commentDTOFromComment(comment);
                    // Check if comment has children and set flag
                    return hasChildrenReactive(comment.getId())
                            .map(hasChildren -> CommentDTO.hasChildren(dto,hasChildren));
                });
    }

    // Better reactive version of hasChildren check
    private Mono<Boolean> hasChildrenReactive(Long commentId) {
        return commentRepository.countByParentCommentId(commentId)
                .map(count -> count > 0);
    }

    // Option 4: Complete reactive solution with proper error handling
    public Flux<CommentDTO> getCommentsTreesPageableReactive(Long recipeId, Pageable pageable) {
        return commentRepository.findByParentCommentIdIsNullAndRecipeIdOrderByCreatedAtDesc(recipeId, pageable)
                .flatMap(rootComment ->
                        loadCommentTreeReactive(rootComment)
                                .onErrorResume(error -> {
                                    // Log error and return just the root comment without children
                                    log.error("Error loading comment tree for comment {}: {}",
                                            rootComment.getId(), error.getMessage());
                                    return Mono.just(CommentDTO.commentDTOFromComment(rootComment));
                                })
                );
    }

    // Reactive tree loading with depth limit to prevent infinite recursion
    private Mono<CommentDTO> loadCommentTreeReactive(Comment comment) {
        return loadCommentTreeReactive(comment, 0, 10);
    }

    private Mono<CommentDTO> loadCommentTreeReactive(Comment comment, int currentDepth, int maxDepth) {
        CommentDTO commentDTO = CommentDTO.commentDTOFromComment(comment);
        if (currentDepth >= maxDepth) {
            return Mono.just(commentDTO);
        }
        return commentRepository.findByParentCommentIdOrderByCreatedAtAsc(comment.getId())
                .flatMap(child -> loadCommentTreeReactive(child, currentDepth + 1, maxDepth))
                .collectList()
                .map(children -> CommentDTO.withList(commentDTO, children));
    }
}
