package md.ctif.recipes_app.service;

import md.ctif.recipes_app.DTO.IngredientDTO;
import md.ctif.recipes_app.entity.RecipeIngredient;
import md.ctif.recipes_app.repository.RecipeIngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RecipeIngredientService {
    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    public Mono<RecipeIngredient> getById(Long id) {
        return recipeIngredientRepository.findById(id);
    }

    public Mono<RecipeIngredient> saveByDTOandId(IngredientDTO ingredientDTO, Long ingredientId, Long recipeId) {
        return recipeIngredientRepository.save(
                new RecipeIngredient(ingredientDTO, ingredientId, recipeId)
        );
    }

    public Mono<RecipeIngredient> saveOrUpdate(IngredientDTO ingredientDTO, Long ingredientId, Long recipeId) {
        return recipeIngredientRepository.findByIngredientIdAndRecipeId(ingredientId, recipeId)
                .flatMap(existingRecipeIngredient -> {
                    existingRecipeIngredient.setAmount(ingredientDTO.amount());
                    existingRecipeIngredient.setMeasure(ingredientDTO.measure());
                    return recipeIngredientRepository.save(existingRecipeIngredient);
                })
                .switchIfEmpty(Mono.defer(() ->
                        recipeIngredientRepository.save(new RecipeIngredient(
                                ingredientDTO, ingredientId, recipeId
                        ))
                ));
    }
}
