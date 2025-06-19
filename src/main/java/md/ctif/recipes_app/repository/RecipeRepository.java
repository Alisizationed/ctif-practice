package md.ctif.recipes_app.repository;

import md.ctif.recipes_app.entity.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface RecipeRepository extends ReactiveCrudRepository<Recipe, Long> {
    Flux<Recipe> findAllBy(Pageable pageable);
    Flux<Recipe> findAllByCreatedBy(String createdBy);
}
