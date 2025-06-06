package md.ctif.recipes_app.DTO;

import md.ctif.recipes_app.entity.Tag;

import java.util.List;

public record RecipeDTO(
        Long id,
        String keycloakId,
        String image,
        String title,
        String description,
        String contents,
        List<Tag> tags,
        List<IngredientDTO> ingredients) {
    public RecipeDTO withImage(String image) {
        return new RecipeDTO(id(), keycloakId(), image, title(), description(), contents(), tags(), ingredients());
    }
}