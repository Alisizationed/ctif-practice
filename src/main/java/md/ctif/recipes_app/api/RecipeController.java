package md.ctif.recipes_app.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import md.ctif.recipes_app.DTO.RecipeDTO;
import md.ctif.recipes_app.service.CustomService;
import md.ctif.recipes_app.service.FileStorageService;
import md.ctif.recipes_app.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {
    @Autowired
    private RecipeService recipeService;
    @Autowired
    private CustomService customService;
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/{id}")
    public Mono<RecipeDTO> getRecipe(@PathVariable Long id) {
        return customService.getById(id);
    }

    @GetMapping("/")
    public Flux<RecipeDTO> getAllRecipes() { return customService.getAll(); }

    @PostMapping(path = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> saveRecipe(
            @RequestPart("image") Mono<FilePart> filePartMono,
            @RequestPart("body") Mono<String> requestBodyMono
    ) {
        ObjectMapper mapper = new ObjectMapper();

        return Mono.zip(filePartMono, requestBodyMono)
                .flatMap(tuple -> {
                    FilePart filePart = tuple.getT1();
                    String json = tuple.getT2();

                    RecipeDTO recipeDTO;
                    try {
                        recipeDTO = mapper.readValue(json, RecipeDTO.class);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }

                    return fileStorageService.saveFile(filePart)
                            .flatMap(filename -> recipeService.save(recipeDTO, filename));
                })
                .map(result -> ResponseEntity.ok("Recipe saved successfully with ID: " + result))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body("Failed to save recipe: " + e.getMessage())));
    }


    @PutMapping(path="/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> updateRecipe(
            @PathVariable Long id,
            @RequestPart("image") FilePart filePart,
            @Parameter(
                    required = true,
                    schema = @Schema(implementation = RecipeDTO.class)
            )
            @RequestPart("body") String requestBodyAsJson
    ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        RecipeDTO recipeDTO = mapper.readValue(requestBodyAsJson, RecipeDTO.class);
        return fileStorageService.saveFile(filePart)
                .flatMap(filename -> recipeService.update(id, recipeDTO, filename))
                .map(result -> ResponseEntity.ok("Recipe updated successfully with ID: " + result));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteRecipe(@PathVariable Long id) {
        return recipeService.deleteById(id);
    }
}
