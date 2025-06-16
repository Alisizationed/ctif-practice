package md.ctif.recipes_app.api;

import md.ctif.recipes_app.DTO.CommentDTO;
import md.ctif.recipes_app.service.CommentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/recipe/comment")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/")
    public Mono<ResponseEntity<String>> addComment(@RequestBody CommentDTO comment) {
        return commentService.createComment(comment);
    }

    @GetMapping("/{recipeId}")
    public Flux<CommentDTO> getCommentsPageable(
            @PathVariable Long recipeId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        return commentService.getCommentsPageable(recipeId, pageable);
    }

    @GetMapping("/v2/{recipeId}")
    public Flux<CommentDTO> getCommentsPageableV2(
            @PathVariable Long recipeId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        return commentService.getCommentsTreesPageableReactive(recipeId,pageable);
    }
}
