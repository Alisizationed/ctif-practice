package md.ctif.recipes_app.repository;

import md.ctif.recipes_app.entity.Recipe;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends ReactiveCrudRepository<Recipe, Long> {
}
