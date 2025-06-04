package md.ctif.recipes_app.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import md.ctif.recipes_app.DTO.UserProfileDTO;
import md.ctif.recipes_app.entity.UserProfile;
import md.ctif.recipes_app.service.FileStorageService;
import md.ctif.recipes_app.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
public class UserProfileController {
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/{id}")
    public Mono<UserProfile> getUserProfile(@PathVariable Long id) {
        return userProfileService.getById(id);
    }

    @GetMapping("/keycloak/{id}")
    public Mono<UserProfile> getUserProfileByKeycloakId(@PathVariable Long id) {
        return userProfileService.getByKeycloakId(id);
    }

    @GetMapping("/")
    public Flux<UserProfile> getAllUserProfiles() {
        return userProfileService.getAll();
    }

    @PostMapping(path = "/")
    public Mono<ResponseEntity<String>> saveUserProfile(
            @RequestBody UserProfileDTO userProfileDTO
    ) {
        return userProfileService.save(userProfileDTO)
                .map(result -> ResponseEntity.ok("User saved successfully with ID: " + result));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> updateUserProfile(
            @PathVariable Long id,
            @RequestBody UserProfileDTO userProfileDTO
    ) throws JsonProcessingException {
        return userProfileService.update(id, userProfileDTO)
                .map(result -> ResponseEntity.ok("User updated successfully with ID: " + result));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteUserProfile(@PathVariable Long id) {
        return userProfileService.deleteById(id);
    }
}
