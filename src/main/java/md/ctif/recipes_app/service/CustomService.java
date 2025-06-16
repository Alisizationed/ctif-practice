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

    public Flux<ShortRecipeDTO> getAllPageable(Long offset, Long size) {
        return shortRecipeRepository.getAllRecipesShortPageable(offset, size);
    }

    public Flux<ShortRecipeDTO> getAllByUser(String id) {
        return shortRecipeRepository.getAllRecipesShortByUser(id);
    }

    public Flux<ShortRecipeDTO> getAllByUserPageable(String id,Long page, Long size) {
        return shortRecipeRepository.getAllRecipesShortByUserPageable(id, page, size);
    }

    public Flux<ShortRecipeDTO> getRecommendedRecipes(Long id, Long size) {
        return shortRecipeRepository.findSimilarRecipes(id,size);
    }

    public Flux<ShortRecipeDTO> getFavouriteRecipes(String id) {
        return accountsService.getFavouriteRecipes(id)
                .flatMap(recipeId -> shortRecipeRepository.getRecipeShortById(recipeId));
    }

    public Flux<ShortRecipeDTO> getFavouriteRecipesPageable(String id, Integer page, Integer size) {
        return accountsService.getFavouriteRecipesPageable(id,page,size)
                .flatMap(recipeId -> shortRecipeRepository.getRecipeShortById(recipeId));
    }
}
