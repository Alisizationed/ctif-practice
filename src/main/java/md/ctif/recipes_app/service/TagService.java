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
    public Mono<Tag> getById(Long id) {
        return tagRepository.findById(id);
    }
    public Flux<Tag> getAll() {
        return tagRepository.findAll();
    }
    public Mono<Tag> save(Tag tag) {
        return tagRepository.save(tag);
    }
    public Mono<Void> delete(Long id) {
        return tagRepository.deleteById(id);
    }
}
