package md.ctif.recipes_app.repository;

import md.ctif.recipes_app.entity.Tag;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TagRepository extends ReactiveCrudRepository<Tag, Long> {
    Mono<Tag> findByTag(String tag);
}
