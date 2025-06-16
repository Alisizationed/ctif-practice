package md.ctif.recipes_app.service;

import lombok.AllArgsConstructor;
import md.ctif.recipes_app.DTO.RecipeDTO;
import md.ctif.recipes_app.entity.Recipe;
import md.ctif.recipes_app.repository.RecipeRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

        return recipeRepository.save(recipe).flatMap(savedRecipe ->
                        Mono.when(
                                saveTags(recipeDTO, savedRecipe),
                                saveIngredients(recipeDTO, savedRecipe),
                                embeddingService.saveEmbedding(recipe.getId(), recipe.toString())
                        ).thenReturn(ResponseEntity.ok("Recipe saved with ID: " + savedRecipe.getId()))
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
                    return updateIngredientsTagsAndRecipe(recipeDTO, existingRecipe)
                            .then(Mono.just(ResponseEntity.ok("Recipe updated")));
                });
    }

    private Mono<Void> updateIngredientsTagsAndRecipe(RecipeDTO recipeDTO, Recipe existingRecipe) {
        return Mono.when(
                        updateTags(recipeDTO, existingRecipe),
                        updateIngredients(recipeDTO, existingRecipe)
                )
                .then(recipeRepository.save(existingRecipe))
                .flatMap(savedRecipe ->
                        embeddingService.updateEmbedding(existingRecipe.getId(), savedRecipe.toString())
                )
                .then();
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

    public Mono<Long> getAllRecipesCount() {
        return recipeRepository.count();
    }

    public Mono<PageImpl<?>> getAllRecipesPage(Pageable pageable) {
        return recipeRepository.findAllBy(pageable)
                .collectList()
                .zipWith(recipeRepository.count())
                .map(p -> new PageImpl<>(p.getT1(), pageable, p.getT2()));
    }

    public Mono<Long> getAllUserRecipesCount(String createdBy) {
        return recipeRepository.findAllByCreatedBy(createdBy).log().count();
    }
}
