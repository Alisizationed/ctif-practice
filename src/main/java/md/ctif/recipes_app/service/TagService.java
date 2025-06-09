package md.ctif.recipes_app.service;

import md.ctif.recipes_app.entity.Tag;
import md.ctif.recipes_app.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TagService {
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private RecipeTagService recipeTagService;

    public Mono<Tag> getById(Long id) {
        return tagRepository.findById(id);
    }

    public Flux<Tag> getAll() {
        return tagRepository.findAll();
    }

    public Mono<Tag> save(String tag, Long recipeId) {
        return tagRepository.findByTag(tag)
                .switchIfEmpty(
                        tagRepository.save(Tag.builder().tag(tag).build())
                )
                .flatMap(savedTag ->
                        recipeTagService.saveById(savedTag.getId(), recipeId)
                                .thenReturn(savedTag)
                );
    }

    public Mono<Void> delete(Long id) {
        return tagRepository.deleteById(id);
    }

    public Mono<Void> update(Flux<Tag> tags, Long recipeId) {
        return recipeTagService.deleteByRecipeId(recipeId)
                .thenMany(tags.flatMap(tag ->
                        tagRepository.findByTag(tag.getTag())
                                .switchIfEmpty(tagRepository.save(tag))
                                .flatMap(savedTag ->
                                        recipeTagService.saveById(savedTag.getId(), recipeId)
                                )
                ))
                .then();
    }

}
