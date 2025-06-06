package md.ctif.recipes_app.service;

import lombok.AllArgsConstructor;
import md.ctif.recipes_app.DTO.RecipeDTO;
import md.ctif.recipes_app.DTO.ShortRecipeDTO;
import md.ctif.recipes_app.repository.CustomRepository;
import md.ctif.recipes_app.repository.ShortRecipeRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class CustomService {
    private CustomRepository customRepository;
    private ShortRecipeRepository shortRecipeRepository;

    public Mono<RecipeDTO> getById(Long id) {
        return customRepository.fetchRecipeDetails(id);
    }

    public Flux<ShortRecipeDTO> getAll() {
        return shortRecipeRepository.getAllRecipesShort();
    }

    public Flux<ShortRecipeDTO> getAllByUser(String id) {
        return shortRecipeRepository.getAllRecipesShortByUser(id);
    }
}
