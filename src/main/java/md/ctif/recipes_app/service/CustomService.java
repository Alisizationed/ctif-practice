package md.ctif.recipes_app.service;

import md.ctif.recipes_app.DTO.RecipeDTO;
import md.ctif.recipes_app.repository.CustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CustomService {
    @Autowired
    private CustomRepository customRepository;
    public Mono<RecipeDTO> getById(Long id) {
        return customRepository.fetchRecipeDetails(id);
    }

    public Flux<RecipeDTO> getAll() {
        return customRepository.getAllRecipes();
    }
}
