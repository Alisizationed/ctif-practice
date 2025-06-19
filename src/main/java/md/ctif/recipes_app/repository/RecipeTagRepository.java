package md.ctif.recipes_app.repository;

import md.ctif.recipes_app.entity.RecipeTag;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RecipeTagRepository extends ReactiveCrudRepository<RecipeTag, Long> {
    Mono<RecipeTag> findByRecipeIdAndTagId(Long recipeId, Long tagId);

    Mono<Void> deleteByRecipeId(Long recipeId);
}
