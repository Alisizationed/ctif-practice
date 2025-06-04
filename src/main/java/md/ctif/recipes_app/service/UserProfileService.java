package md.ctif.recipes_app.service;

import md.ctif.recipes_app.DTO.UserProfileDTO;
import md.ctif.recipes_app.entity.UserProfile;
import md.ctif.recipes_app.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserProfileService {
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private FileStorageService fileStorageService;

    public Mono<UserProfile> getById(Long id) {
        return userProfileRepository.findById(id);
    }

    public Flux<UserProfile> getAll() {
        return userProfileRepository.findAll();
    }

    public Mono<ResponseEntity<String>> save(UserProfileDTO userProfileDTO) {
        UserProfile userProfile = UserProfile.builder()
                .keycloakId(userProfileDTO.keycloakId())
                .firstName(userProfileDTO.firstName())
                .lastName(userProfileDTO.lastName())
                .bio(userProfileDTO.bio())
                .profilePicture(userProfileDTO.profilePicture())
                .build();
        return userProfileRepository.save(userProfile)
                .map(saved -> ResponseEntity.ok("User saved with ID: " + saved.getId()))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage())));
    }

    public Mono<ResponseEntity<String>> update(Long id, UserProfileDTO userProfileDTO) {
        return userProfileRepository.findById(id)
                .flatMap(user -> {
                    user.setKeycloakId(user.getKeycloakId());
                    user.setFirstName(userProfileDTO.firstName());
                    user.setLastName(userProfileDTO.lastName());
                    user.setBio(userProfileDTO.bio());
                    fileStorageService.delete(user.getProfilePicture());
                    user.setProfilePicture(userProfileDTO.profilePicture());
                    return userProfileRepository.save(user);
                })
                .map(saved -> ResponseEntity.ok("User updated with ID: " + saved.getId()))
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage())));
    }

    public Mono<Void> deleteById(Long id) {
        return userProfileRepository.deleteById(id);
    }

    public Mono<UserProfile> getByKeycloakId(Long id) {
        return userProfileRepository.findByKeycloakId(id);
    }
}
