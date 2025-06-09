package md.ctif.recipes_app.repository;

import md.ctif.recipes_app.entity.RecipeIngredient;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RecipeIngredientRepository extends ReactiveCrudRepository<RecipeIngredient, Long> {
    Mono<RecipeIngredient> findByIngredientIdAndRecipeId(Long ingredientId, Long recipeId);

    Mono<Void> deleteByRecipeId(Long recipeId);
}
