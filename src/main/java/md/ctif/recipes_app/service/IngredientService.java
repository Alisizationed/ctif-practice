package md.ctif.recipes_app.service;

import md.ctif.recipes_app.DTO.IngredientDTO;
import md.ctif.recipes_app.entity.Ingredient;
import md.ctif.recipes_app.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class IngredientService {
    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private RecipeIngredientService recipeIngredientService;

    public Mono<Ingredient> getIngredientById(Long id) {
        return ingredientRepository.findById(id);
    }

    public Flux<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    public Mono<Ingredient> save(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }

    public Mono<Void> update(Long recipeId, Flux<IngredientDTO> ingredientDTOs) {
        return recipeIngredientService.deleteByRecipeId(recipeId)
                .thenMany(ingredientDTOs.flatMap(ingredientDTO ->
                        ingredientRepository.findByIngredient(ingredientDTO.ingredient())
                                .switchIfEmpty(ingredientRepository.save(
                                        Ingredient.builder()
                                                .ingredient(ingredientDTO.ingredient())
                                                .build()
                                )).flatMap(savedIngredient ->
                                        recipeIngredientService.saveByDTOandId(
                                                ingredientDTO, savedIngredient.getId(), recipeId
                                        )
                                )
                ))
                .then();
    }

    public Mono<Void> deleteById(Long id) {
        return ingredientRepository.deleteById(id);
    }

    public Mono<Ingredient> saveByDTOAndId(IngredientDTO ingredientDTO, Long id) {
        return ingredientRepository.findByIngredient(ingredientDTO.ingredient())
                .switchIfEmpty(
                        ingredientRepository.save(
                                Ingredient.builder()
                                        .ingredient(ingredientDTO.ingredient())
                                        .build())
                )
                .flatMap(savedIngredient ->
                        recipeIngredientService
                                .saveByDTOandId(ingredientDTO, savedIngredient.getId(), id)
                                .thenReturn(savedIngredient)
                );
    }
}
