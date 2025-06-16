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
    private AccountsService accountsService;

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

    public Flux<ShortRecipeDTO> getRecommendedRecipes(Long id, Long limit) {
        return shortRecipeRepository.findSimilarRecipes(id,limit);
    }

    public Flux<ShortRecipeDTO> getFavouriteRecipes(String id) {
        return accountsService.getFavouriteRecipes(id)
                .flatMap(recipeId -> shortRecipeRepository.getRecipeShortById(recipeId));
    }

    public Flux<ShortRecipeDTO> getFavouriteRecipesPageable(String id, Integer offset, Integer limit) {
        return accountsService.getFavouriteRecipesPageable(id,offset,limit)
                .flatMap(recipeId -> shortRecipeRepository.getRecipeShortById(recipeId));
    }
}
