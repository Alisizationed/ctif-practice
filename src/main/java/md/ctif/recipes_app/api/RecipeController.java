package md.ctif.recipes_app.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import md.ctif.recipes_app.DTO.RecipeDTO;
import md.ctif.recipes_app.DTO.ShortRecipeDTO;
import md.ctif.recipes_app.repository.RecipeRepository;
import md.ctif.recipes_app.service.CustomService;
import md.ctif.recipes_app.service.FileStorageService;
import md.ctif.recipes_app.service.RecipeService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/api/recipe")
public class RecipeController {
    private RecipeService recipeService;
    private CustomService customService;
    private FileStorageService fileStorageService;

    @GetMapping("/{id}")
    public Mono<RecipeDTO> getRecipe(@PathVariable Long id) {
        return customService.getById(id);
    }

    @GetMapping("/recommended/{id}/{limit}")
    public Flux<RecipeDTO> getRecommendedRecipes(
            @PathVariable Long id,
            @PathVariable Long limit
    ) {
        return customService.getRecommendedRecipes(id, limit);
    }

    @GetMapping("/")
    public Flux<ShortRecipeDTO> getAllRecipes() {
        return customService.getAll();
    }

    @GetMapping("/pageable")
    public Flux<ShortRecipeDTO> getAllRecipesPageable(
            @RequestParam Long page,
            @RequestParam Long size
    ) {
        return customService.getAllPageable(page,size);
    }
    @GetMapping("/pageable/v2")
    public Mono<PageImpl<?>> getAllRecipesPageableV2(
            @RequestParam int page,
            @RequestParam int size
    ) {
        Pageable pageable =  PageRequest.of(page, size);
        return recipeService.getAllRecipesPage(pageable);
    }

    @GetMapping("/count")
    public Mono<Long> getAllRecipesCount() {
        return recipeService.getAllRecipesCount();
    }

    @GetMapping("/user/{id}")
    public Flux<ShortRecipeDTO> getAllUsersRecipes(@PathVariable String id) {
        return customService.getAllByUser(id);
    }

    @PostMapping(path = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> saveRecipe(
            @RequestPart("image") Mono<FilePart> filePartMono,
            @RequestPart("body") Mono<String> requestBodyMono
    ) {
        return requestBodyMono
                .flatMap(json -> parseRecipe(json)
                        .flatMap(recipe -> filePartMono.flatMap(fileStorageService::saveFile)
                                .map(recipe::withImage))
                        .flatMap(recipeService::save))
                .map(result -> ResponseEntity.ok("Recipe saved successfully with ID: " + result))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body("Failed to save recipe: " + e.getMessage())));
    }

    private Mono<RecipeDTO> parseRecipe(String json) {
        try {
            return Mono.just(new ObjectMapper().readValue(json, RecipeDTO.class));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> updateRecipe(
            @PathVariable Long id,
            @RequestPart(name = "image", required = false) FilePart filePart,
            @Parameter(
                    required = true,
                    schema = @Schema(implementation = RecipeDTO.class)
            )
            @RequestPart("body") String requestBodyAsJson
    ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        RecipeDTO recipeDTO = mapper.readValue(requestBodyAsJson, RecipeDTO.class);

        if (filePart != null) {
            return fileStorageService.saveFile(filePart)
                    .flatMap(filename -> recipeService.update(id, recipeDTO, filename));
        } else {
            return recipeService.update(id, recipeDTO, null);
        }
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteRecipe(@PathVariable Long id) {
        return recipeService.deleteById(id);
    }

    @GetMapping("/favourites/{id}")
    public Flux<ShortRecipeDTO> getFavouriteRecipes(@PathVariable String id) {
        return customService.getFavouriteRecipes(id);
    }

    @GetMapping("/favourites/v2/{id}")
    public Flux<ShortRecipeDTO> getFavouriteRecipesPageable(
            @PathVariable String id,
            @RequestParam Integer offset,
            @RequestParam Integer limit
            ) {
        return customService.getFavouriteRecipesPageable(id, offset, limit);
    }
}
