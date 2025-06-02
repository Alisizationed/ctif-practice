package md.ctif.recipes_app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import md.ctif.recipes_app.DTO.RecipeDTO;
import md.ctif.recipes_app.entity.*;
import md.ctif.recipes_app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RecipeService {
    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private RecipeTagRepository recipeTagRepository;
    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    //                .flatMap(savedRecipe ->
//                        Flux.fromIterable(recipeDTO.tags())
//                                .flatMap(tag -> tagService.save(tag)
//                                        .then(
//                                            recipeTagService.save(RecipeTag
//                                                .builder()
//                                                .recipeId(savedRecipe.getId())
//                                                .tagId(tag.getId())
//                                                .build()
//                                        ))
//                                )
//                                .then(Mono.just(savedRecipe))
//                )
    public Mono<ResponseEntity<String>> save(RecipeDTO recipeDTO, String imagePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        Recipe recipe = null;
        try {
            recipe = Recipe.builder()
                    .userProfileId(recipeDTO.userProfileId())
                    .title(recipeDTO.title())
                    .description(recipeDTO.description())
                    .contents(objectMapper.readTree(recipeDTO.contents()))
                    .image(imagePath)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return recipeRepository.save(recipe)
                .flatMap(savedRecipe -> {
                    Mono<Void> tagFlow = Flux.fromIterable(recipeDTO.tags())
                            .flatMap(tagDTO -> {
                                Tag tag = Tag.builder().tag(tagDTO.getTag()).build();
                                return tagRepository.save(tag)
                                        .flatMap(savedTag -> {
                                            RecipeTag rt = RecipeTag.builder()
                                                    .recipeId(savedRecipe.getId())
                                                    .tagId(savedTag.getId())
                                                    .build();
                                            return recipeTagRepository.save(rt);
                                        });
                            })
                            .then();
                    Mono<Void> ingredientFlow = Flux.fromIterable(recipeDTO.ingredients())
                            .flatMap(ingredientDTO -> {
                                Ingredient ingredient = Ingredient.builder()
                                        .ingredient(ingredientDTO.name())
                                        .build();
                                return ingredientRepository.save(ingredient)
                                        .flatMap(savedIngredient -> {
                                            RecipeIngredient ri = RecipeIngredient.builder()
                                                    .recipeId(savedRecipe.getId())
                                                    .ingredientId(savedIngredient.getId())
                                                    .amount(ingredientDTO.quantity())
                                                    .measure(ingredientDTO.measure())
                                                    .build();
                                            return recipeIngredientRepository.save(ri);
                                        });
                            })
                            .then();
                    return Mono.when(tagFlow, ingredientFlow)
                            .thenReturn(ResponseEntity.ok("Recipe saved with ID: " + savedRecipe.getId()));
                })
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage()))
                );
    }

    public Flux<Recipe> getAll() {
        return recipeRepository.findAll();
    }

    public Mono<Recipe> getById(long id) {
        return recipeRepository.findById(id);
    }
//    public Mono<Recipe> getRecipeWithDetails(Long recipeId) {
//        Mono<Recipe> recipeMono = recipeRepository.findById(recipeId);
//        Mono<List<ContentBlock>> contentBlocksMono = contentBlockRepository
//                .findByRecipeId(recipeId)
//                .collectList();
//        Mono<List<Tag>> tagsMono = tagRepository
//                .findTagsByRecipeId(recipeId)
//                .collectList();
//        Mono<List<RecipeIngredient>> recipeIngredientsMono = recipeIngredientRepository
//                .findByRecipeId(recipeId)
//                .collectList();
//        Mono<List<Ingredient>> ingredientsMono = recipeIngredientsMono
//                .flatMap(recipeIngredients -> {
//                    List<Long> ingredientIds = recipeIngredients.stream()
//                            .map(RecipeIngredient::getIngredientId)
//                            .collect(Collectors.toList());
//                    return ingredientRepository.findAllById(ingredientIds)
//                            .collectList();
//                });
//        return Mono.zip(recipeMono, contentBlocksMono, tagsMono, ingredientsMono)
//                .map(tuple -> {
//                    Recipe recipe = tuple.getT1();
//                    List<ContentBlock> contentBlocks = tuple.getT2();
//                    List<Tag> tags = tuple.getT3();
//                    List<Ingredient> ingredients = tuple.getT4();
//                    recipe.setContentBlocks(contentBlocks);
//                    recipe.setTags(tags);
//                    recipe.setIngredients(ingredients);
//                    return recipe;
//                });
//    }

    public Mono<Void> deleteById(long id) {
        return recipeRepository.deleteById(id);
    }

    public Mono<ResponseEntity<String>> update(long id, RecipeDTO recipeDTO, String imagePath) {
        return recipeRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Recipe not found")))
                .flatMap(existingRecipe -> {
                    existingRecipe.setTitle(recipeDTO.title());
                    existingRecipe.setDescription(recipeDTO.description());
                    if (!imagePath.equals(existingRecipe.getImage())) {
                        fileStorageService.delete(existingRecipe.getImage());
                        existingRecipe.setImage(imagePath);
                    }
                    existingRecipe.setUserProfileId(recipeDTO.userProfileId());

//                    return contentBlockService.deleteByRecipeId(existingRecipe.getId())
//                            .thenMany(Flux.fromIterable(recipeDTO.contentBlocks())
//                                    .index()
//                                    .flatMap(tuple -> contentBlockService.save(tuple.getT2(), existingRecipe.getId(), tuple.getT1() + 1)))
//                            .then(recipeRepository.save(existingRecipe))
                    return recipeRepository.save(existingRecipe)
                            .map(saved -> ResponseEntity.ok("Recipe updated with ID: " + saved.getId()));
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Update failed: " + e.getMessage())));
    }
}
