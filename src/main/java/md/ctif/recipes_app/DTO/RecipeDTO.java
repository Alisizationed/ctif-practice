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
}