package md.ctif.recipes_app.repository;

import md.ctif.recipes_app.entity.Ingredient;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface IngredientRepository extends ReactiveCrudRepository<Ingredient, Long> {
    Mono<Ingredient> findByIngredient(String ingredient);
}
