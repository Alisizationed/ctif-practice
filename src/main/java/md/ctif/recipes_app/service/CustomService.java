package md.ctif.recipes_app.service;

import lombok.AllArgsConstructor;
import md.ctif.recipes_app.DTO.RecipeDTO;
import md.ctif.recipes_app.DTO.ShortRecipeDTO;
import md.ctif.recipes_app.repository.CustomRepository;
import md.ctif.recipes_app.repository.RecommendationsCustomRepository;
import md.ctif.recipes_app.repository.ShortRecipeRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class CustomService {
    private final RecommendationsCustomRepository recommendationsCustomRepository;
    private CustomRepository customRepository;
    private ShortRecipeRepository shortRecipeRepository;

    public Mono<RecipeDTO> getById(Long id) {
        return customRepository.fetchRecipeDetails(id);
    }

    public Flux<ShortRecipeDTO> getAll() {
        return shortRecipeRepository.getAllRecipesShort();
    }

    public Flux<ShortRecipeDTO> getAllPageable(Long offset, Long limit) {
        return shortRecipeRepository.getAllRecipesShortPageable(offset, limit);
    }

    public Flux<ShortRecipeDTO> getAllByUser(String id) {
        return shortRecipeRepository.getAllRecipesShortByUser(id);
    }

    public Flux<RecipeDTO> getRecommendedRecipes(Long id, Long limit) {
        return recommendationsCustomRepository.findSimilarRecipes(id,limit);
    }
}
