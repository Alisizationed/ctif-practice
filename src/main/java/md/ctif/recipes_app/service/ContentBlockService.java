package md.ctif.recipes_app.service;

import md.ctif.recipes_app.DTO.ContentBlockDTO;
import md.ctif.recipes_app.DTO.ImageBlockDTO;
import md.ctif.recipes_app.DTO.ParagraphBlockDTO;
import md.ctif.recipes_app.entity.ContentBlock;
import md.ctif.recipes_app.repository.ContentBlockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ContentBlockService {
    @Autowired
    private ContentBlockRepository contentBlockRepository;
    public Mono<ContentBlock> findById(Long id) {
        return contentBlockRepository.findById(id);
    }
    public Flux<ContentBlock> findByRecipeId(Long recipeId) {
        return contentBlockRepository.findByRecipeId(recipeId);
    }
    public Flux<ContentBlock> getAll(Long id) {
        return contentBlockRepository.findAll();
    }
    public Mono<ContentBlock> save(ContentBlockDTO contentBlockDTO, Long recipeId, Long position) {
        if (contentBlockDTO instanceof ImageBlockDTO imageBlock) {
            String url = imageBlock.getData().getFile().getUrl();
            if (!url.startsWith("/uploads/")) {
                return Mono.error(new RuntimeException("Untrusted image URL"));
            }
        }

        ContentBlock contentBlock = ContentBlock.builder()
            .recipeId(recipeId)
            .type(contentBlockDTO instanceof ImageBlockDTO ? "image" : "text")
            .text(contentBlockDTO instanceof ParagraphBlockDTO paragraphBlock ? paragraphBlock.getData().getText() : "")
            .url(contentBlockDTO instanceof ImageBlockDTO imageBlock ? imageBlock.getData().getFile().getUrl() : "")
            .position(position)
            .build();

        return contentBlockRepository.save(contentBlock);
    }
    public Mono<ContentBlock> update(Long id, ContentBlockDTO contentBlockDTO) {
        return contentBlockRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Content block not found")))
                .flatMap(existingContentBlock -> {
                    if(contentBlockDTO instanceof ImageBlockDTO imageBlockDTO) {
                        existingContentBlock.setType(imageBlockDTO.getType());
                        existingContentBlock.setUrl(imageBlockDTO.getData().getFile().getUrl());
                    }
                    if(contentBlockDTO instanceof ParagraphBlockDTO paragraphBlockDTO) {
                        existingContentBlock.setType(paragraphBlockDTO.getType());
                        existingContentBlock.setText(paragraphBlockDTO.getData().getText());
                    }
                    return contentBlockRepository.save(existingContentBlock);
                });
    }
    public Mono<Void> deleteById(Long id) {
        return contentBlockRepository.deleteById(id);
    }
    public Mono<Void> deleteByRecipeId(Long recipeId) {
        return contentBlockRepository.deleteByRecipeId(recipeId);
    }
}
