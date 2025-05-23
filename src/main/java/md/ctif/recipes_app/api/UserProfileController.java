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

    @GetMapping("/")
    public Flux<UserProfile> getAllUserProfiles() {
        return userProfileService.getAll();
    }

    @PostMapping(path="/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> saveUserProfile(
            @RequestPart("files") FilePart filePart,
            @Parameter(
                    required = true,
                    schema = @Schema(implementation = UserProfileDTO.class)
            )
            @RequestPart("body") String requestBodyAsJson
    ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        UserProfileDTO userProfileDTO = mapper.readValue(requestBodyAsJson, UserProfileDTO.class);
        return fileStorageService.saveFile(filePart)
                .flatMap(filename -> userProfileService.save(userProfileDTO, filename))
                .map(result -> ResponseEntity.ok("User saved successfully with ID: " + result));
    }

    @PutMapping(path="/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> updateUserProfile(
            @PathVariable Long id,
            @RequestPart("files") FilePart filePart,
            @Parameter(
                    required = true,
                    schema = @Schema(implementation = UserProfileDTO.class)
            )
            @RequestPart("body") String requestBodyAsJson
    ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        UserProfileDTO userProfileDTO = mapper.readValue(requestBodyAsJson, UserProfileDTO.class);
        return fileStorageService.saveFile(filePart)
                .flatMap(filename -> userProfileService.update(id, userProfileDTO, filename))
                .map(result -> ResponseEntity.ok("User updated successfully with ID: " + result));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteUserProfile(@PathVariable Long id) {
        return userProfileService.deleteById(id);
    }
}
