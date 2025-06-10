package md.ctif.recipes_app.service;

import lombok.AllArgsConstructor;
import md.ctif.recipes_app.DTO.RecipeDTO;
import md.ctif.recipes_app.entity.Recipe;
import md.ctif.recipes_app.repository.RecipeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final TagService tagService;
    private final IngredientService ingredientService;
    private final EmbeddingService embeddingService;

    public Mono<ResponseEntity<String>> save(RecipeDTO recipeDTO) {
        Recipe recipe = new Recipe(recipeDTO);

        return embeddingService.generateEmbedding(recipe.toString())
                .flatMap(embedding -> recipeRepository.save(recipe).flatMap(savedRecipe ->
                                Mono.when(
                                        saveTags(recipeDTO, savedRecipe),
                                        saveIngredients(recipeDTO, savedRecipe),
                                        embeddingService.saveEmbedding(recipe.getId(), recipe.toString())
                                ).thenReturn(ResponseEntity.ok("Recipe saved with ID: " + savedRecipe.getId()))
                        )
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
                .switchIfEmpty(Mono.error(new RuntimeException("Recipe not found with ID: " + id)))
                .flatMap(existingRecipe -> {
                    existingRecipe.update(recipeDTO.withImage(imagePath));
                    return updateIngredientsTagsAndRecipe(recipeDTO, existingRecipe);
                })
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage()))
                );
    }

    private Mono<ResponseEntity<String>> updateIngredientsTagsAndRecipe(RecipeDTO recipeDTO, Recipe existingRecipe) {
        return Mono.when(
                        updateTags(recipeDTO, existingRecipe),
                        updateIngredients(recipeDTO, existingRecipe)
                )
                .then(recipeRepository.save(existingRecipe))
                .map(savedRecipe -> ResponseEntity.ok("Recipe updated with ID: " + savedRecipe.getId()));
    }

    private Mono<Void> updateTags(RecipeDTO recipeDTO, Recipe existingRecipe) {
        return tagService.update(
                Flux.fromIterable(recipeDTO.tags()),
                existingRecipe.getId()
        ).then();
    }

    private Mono<Void> updateIngredients(RecipeDTO recipeDTO, Recipe existingRecipe) {
        return ingredientService.update(
                existingRecipe.getId(),
                Flux.fromIterable(recipeDTO.ingredients())
        ).then();
    }
}
