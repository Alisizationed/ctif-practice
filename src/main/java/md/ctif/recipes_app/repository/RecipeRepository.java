package md.ctif.recipes_app.repository;

import com.fasterxml.jackson.databind.JsonNode;
import md.ctif.recipes_app.entity.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RecipeRepository extends ReactiveCrudRepository<Recipe, Long> {
    Mono<Recipe> findRecipeByTitleAndDescriptionAndContentsAndImage(String title, String description, JsonNode contents, String image);
    Flux<Recipe> findAllBy(Pageable pageable);
    Flux<Recipe> findAllByCreatedBy(String createdBy);
}
