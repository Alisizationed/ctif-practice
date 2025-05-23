package md.ctif.recipes_app.repository;

import md.ctif.recipes_app.entity.RecipeTag;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeTagRepository extends ReactiveCrudRepository<RecipeTag, Long> {
}
