package md.ctif.recipes_app.service;

import lombok.AllArgsConstructor;
import md.ctif.recipes_app.DTO.RecipeDTO;
import md.ctif.recipes_app.entity.*;
import md.ctif.recipes_app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class RecipeService {
    private final  RecipeRepository recipeRepository;
    private final FileStorageService fileStorageService;
    private final TagService tagService;
    private final IngredientService ingredientService;

    public Mono<ResponseEntity<String>> save(RecipeDTO recipeDTO) {

        Recipe recipe = new Recipe(recipeDTO);

        return recipeRepository.save(recipe)
                .flatMap(savedRecipe ->
                    Mono.when(saveTags(recipeDTO, savedRecipe), saveIngredients(recipeDTO, savedRecipe))
                            .thenReturn(ResponseEntity.ok("Recipe saved with ID: " + savedRecipe.getId()))
                )
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage()))
                );
    }

    private Mono<Void> saveIngredients(RecipeDTO recipeDTO, Recipe savedRecipe) {
        return Flux.fromIterable(recipeDTO.ingredients())
                .flatMap(ingredientDTO -> ingredientService
                        .saveByDTOAndId(ingredientDTO, savedRecipe.getId()))
                .then();
    }

    private Mono<Void> saveTags(RecipeDTO recipeDTO, Recipe savedRecipe) {
        return Flux.fromIterable(recipeDTO.tags())
                .flatMap(tagDTO -> tagService.save(tagDTO.getTag(), savedRecipe.getId()))
                .then();
    }

    public Flux<Recipe> getAll() {
        return recipeRepository.findAll();
    }

    public Mono<Recipe> getById(Long id) {
        return recipeRepository.findById(id);
    }

    public Mono<Void> deleteById(Long id) {
        return recipeRepository.deleteById(id);
    }

    public Mono<ResponseEntity<String>> update(Long id, RecipeDTO recipeDTO, String imagePath) {
        return recipeRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Recipe not found")))
                .flatMap(existingRecipe -> {
                    existingRecipe.setTitle(recipeDTO.title());
                    existingRecipe.setDescription(recipeDTO.description());
                    if (!imagePath.equals(existingRecipe.getImage())) {
                        fileStorageService.delete(existingRecipe.getImage());
                        existingRecipe.setImage(imagePath);
                    }
                    existingRecipe.setKeycloakId(recipeDTO.keycloakId());

                    return recipeRepository.save(existingRecipe)
                            .map(saved -> ResponseEntity.ok("Recipe updated with ID: " + saved.getId()));
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Update failed: " + e.getMessage())));
    }
}
