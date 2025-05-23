package md.ctif.recipes_app.service;

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

    public Mono<Ingredient> getIngredientById(Long id) {
        return ingredientRepository.findById(id);
    }

    public Flux<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    public Mono<Ingredient> save(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }

    public Mono<Ingredient> update(Long id, Ingredient ingredient) {
        return ingredientRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Not found")))
                .flatMap(ingredient1 -> {
                    ingredient1.setIngredient(ingredient.getIngredient());
                    return ingredientRepository.save(ingredient1);
                });
    }

    public Mono<Void> deleteById(Long id) {
        return ingredientRepository.deleteById(id);
    }
}
