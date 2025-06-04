package md.ctif.recipes_app.repository;

import md.ctif.recipes_app.entity.UserProfile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserProfileRepository extends ReactiveCrudRepository<UserProfile, Long> {
    Mono<UserProfile> findByKeycloakId(Long id);
}
