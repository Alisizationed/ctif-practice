package md.ctif.recipes_app.DTO;

public record ShortRecipeDTO(
        Long id,
        String keycloakId,
        String image,
        String title) {
}