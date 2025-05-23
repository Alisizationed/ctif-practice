package md.ctif.recipes_app.repository;

import md.ctif.recipes_app.entity.RecipeIngredient;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface RecipeIngredientRepository extends ReactiveCrudRepository<RecipeIngredient, Long> {
    Flux<RecipeIngredient> findByRecipeId(Long recipeId);
}
