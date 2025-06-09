package md.ctif.recipes_app.repository;

import io.r2dbc.spi.Row;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.ctif.recipes_app.DTO.IngredientDTO;
import md.ctif.recipes_app.DTO.RecipeDTO;
import md.ctif.recipes_app.entity.Tag;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class CustomRepository {
    private final DatabaseClient client;
    private static final String GET_RECIPE_SQL = """
                    SELECT 
                      r.id AS r_id, r.created_by, r.title, r.description, r.image AS r_image, r.contents,
                      t.id AS tag_id, t.tag AS tag_name,
                      i.id AS ing_id, i.ingredient AS ing_name, ri.amount AS ing_quantity, ri.measure AS ing_measure
                    FROM recipe r
                    LEFT JOIN recipe_tag rt ON rt.recipe_id = r.id
                    LEFT JOIN tag t ON t.id = rt.tag_id
                    LEFT JOIN recipe_ingredient ri ON ri.recipe_id = r.id
                    LEFT JOIN ingredient i ON i.id = ri.ingredient_id
                """;

    public Mono<RecipeDTO> fetchRecipeDetails(Long recipeId) {
        Flux<FlatRecipeRow> flatRows = getFlatRecipeRowFlux(recipeId);
        return flatRows.collectList()
                .mapNotNull(this::getRecipeDTO);
    }

    private Flux<FlatRecipeRow> getFlatRecipeRowFlux(Long recipeId) {
        return client.sql(GET_RECIPE_SQL + " WHERE r.id = $1")
                .bind(0, recipeId)
                .map((row, meta) -> getFlatRecipeRow(row))
                .all();
    }

    private FlatRecipeRow getFlatRecipeRow(Row row) {
        return new FlatRecipeRow(
                row.get("r_id", Long.class),
                row.get("created_by", String.class),
                row.get("title", String.class),
                row.get("description", String.class),
                row.get("r_image", String.class),
                row.get("contents", String.class),
                row.get("tag_id", Long.class),
                row.get("tag_name", String.class),
                row.get("ing_id", Long.class),
                row.get("ing_name", String.class),
                row.get("ing_quantity", Long.class),
                row.get("ing_measure", String.class)
        );
    }

    private RecipeDTO getRecipeDTO(List<FlatRecipeRow> rows) {
        if (rows.isEmpty()) return null;

        FlatRecipeRow first = rows.getFirst();

        Map<Long, Tag> tags = new LinkedHashMap<>();
        Map<Long, IngredientDTO> ingredients = new LinkedHashMap<>();

        for (FlatRecipeRow row : rows) {
            if (row.getTagId() != null && !tags.containsKey(row.getTagId())) {
                tags.put(row.getTagId(), new Tag(row.getTagId(), row.getTagName()));
            }
            if (row.getIngredientId() != null && !ingredients.containsKey(row.getIngredientId())) {
                ingredients.put(row.getIngredientId(),
                        new IngredientDTO(row.getIngredientId(), row.getIngredientName(), row.getIngredientQuantity(), row.getIngredientMeasure()));
            }
        }

        return new RecipeDTO(
                first.getRecipeId(),
                first.getKeycloakId(),
                first.getImageUrl(),
                first.getTitle(),
                first.getDescription(),
                first.getContents(),
                new ArrayList<>(tags.values()),
                new ArrayList<>(ingredients.values())
        );
    }

    public Flux<RecipeDTO> getAllRecipes() {
        Flux<FlatRecipeRow> flatRows = getFlatRecipeRowFlux();

        return flatRows
                .groupBy(FlatRecipeRow::getRecipeId)
                .flatMap(group -> group.collectList().mapNotNull(this::getRecipeDTO));
    }

    private Flux<FlatRecipeRow> getFlatRecipeRowFlux() {
        return client.sql(GET_RECIPE_SQL)
                .map((row, meta) -> getFlatRecipeRow(row))
                .all();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class FlatRecipeRow {
        private Long recipeId;
        private String keycloakId;
        private String title;
        private String description;
        private String imageUrl;
        private String contents;

        private Long tagId;
        private String tagName;

        private Long ingredientId;
        private String ingredientName;
        private Long ingredientQuantity;
        private String ingredientMeasure;
    }
}
