package md.ctif.recipes_app.service;

import md.ctif.recipes_app.entity.RecipeTag;
import md.ctif.recipes_app.repository.RecipeTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RecipeTagService {
    @Autowired
    private RecipeTagRepository recipeTagRepository;
    public Mono<RecipeTag> getById(Long id) {
        return recipeTagRepository.findById(id);
    }
    public Flux<RecipeTag> getAll() {
        return recipeTagRepository.findAll();
    }
    public Mono<RecipeTag> save(RecipeTag recipeTag) {
        return recipeTagRepository.save(recipeTag);
    }
}
