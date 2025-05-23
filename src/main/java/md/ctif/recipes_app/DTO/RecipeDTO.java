package md.ctif.recipes_app.DTO;

import md.ctif.recipes_app.entity.Tag;

import java.util.List;

public record RecipeDTO (Long id, Long userProfileId, String image, String title, String description, List<ContentBlockDTO> contentBlocks, List<Tag> tags, List<IngredientDTO> ingredients) {}