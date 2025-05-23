package md.ctif.recipes_app.service;

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
}
