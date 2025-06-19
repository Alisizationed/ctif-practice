package md.ctif.recipes_app.service;

import md.ctif.recipes_app.entity.RecipeTag;
import md.ctif.recipes_app.repository.RecipeTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RecipeTagService {
    @Autowired
    private RecipeTagRepository recipeTagRepository;

    public Mono<RecipeTag> saveById(Long id, Long recipeId) {
        return recipeTagRepository.save(
                RecipeTag.builder()
                        .tagId(id)
                        .recipeId(recipeId)
                        .build()
        );
    }

    public Mono<Void> deleteByRecipeId(Long recipeId) {
        return recipeTagRepository.deleteByRecipeId(recipeId);
    }
}
