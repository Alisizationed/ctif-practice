package md.ctif.recipes_app.repository;

import md.ctif.recipes_app.entity.Tag;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TagRepository extends ReactiveCrudRepository<Tag, Long> {
    @Query("SELECT * FROM tag WHERE id = ANY(SELECT tag_ig FROM recipe_tag WHERE recipe_id = :recipeId)")
    Flux<Tag> findTagsByRecipeId(Long recipeId);
}
