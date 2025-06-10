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
import java.util.Objects;

@AllArgsConstructor
@Data
@Repository
public class RecommendationsCustomRepository {

    private static final String BASE_RECIPE_SELECT_SQL = """
                SELECT
                  r.id AS r_id, r.created_by, r.title, r.description, r.image AS r_image, r.contents,
                  t.id AS tag_id, t.tag AS tag_name,
                  i.id AS ing_id, i.ingredient AS ing_name, ri.amount AS ing_quantity, ri.measure AS ing_measure,
                  e.embedding
                FROM recipe r
                LEFT JOIN recipe_tag rt ON rt.recipe_id = r.id
                LEFT JOIN tag t ON t.id = rt.tag_id
                LEFT JOIN recipe_ingredient ri ON ri.recipe_id = r.id
                LEFT JOIN ingredient i ON i.id = ri.ingredient_id
                LEFT JOIN embeddings e ON r.id = e.recipe_id
            """;

    private static final String SELECT_EMBEDDING_SQL = """
            SELECT embedding::float4[] AS embedding
            FROM embeddings
            WHERE recipe_id = :recipeId
            """;

    private final DatabaseClient client;

    public Mono<RecipeDTO> fetchRecipeDetails(Long recipeId,Long limit) {
        return client.sql(BASE_RECIPE_SELECT_SQL + " WHERE r.id = :recipeId")
                .bind("recipeId", recipeId)
                .map((row, meta) -> getFlatRecipeRow(row))
                .all()
                .collectList()
                .mapNotNull(this::getRecipeDTO);
    }

    public Flux<RecipeDTO> findSimilarRecipes(Long recipeId, Long limit) {
        return client.sql(SELECT_EMBEDDING_SQL)
                .bind("recipeId", recipeId)
                .map(row -> row.get("embedding", Float[].class))
                .one()
                .flatMapMany(targetEmbeddingFloats -> {
                    Objects.requireNonNull(targetEmbeddingFloats, "Embedding not found for recipe ID: " + recipeId);

                    String similarRecipesSql = BASE_RECIPE_SELECT_SQL + """
                        WHERE r.id != :recipeId
                        ORDER BY e.embedding <-> (:targetEmbedding::vector)
                        LIMIT :limit
                        """;

                    return client.sql(similarRecipesSql)
                            .bind("recipeId", recipeId)
                            .bind("targetEmbedding", targetEmbeddingFloats)
                            .bind("limit", limit)
                            .map((row, meta) -> getFlatRecipeRow(row))
                            .all()
                            .groupBy(FlatRecipeRow::getRecipeId)
                            .flatMap(group -> group.collectList().mapNotNull(this::getRecipeDTO));
                })
                .switchIfEmpty(Flux.empty());
    }

    public Flux<RecipeDTO> getAllRecipes() {
        return client.sql(BASE_RECIPE_SELECT_SQL)
                .map((row, meta) -> getFlatRecipeRow(row))
                .all()
                .groupBy(FlatRecipeRow::getRecipeId)
                .flatMap(group -> group.collectList().mapNotNull(this::getRecipeDTO));
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
        if (rows.isEmpty()) {
            return null;
        }

        FlatRecipeRow firstRow = rows.get(0);

        Map<Long, Tag> tagsMap = new LinkedHashMap<>();
        Map<Long, IngredientDTO> ingredientsMap = new LinkedHashMap<>();

        for (FlatRecipeRow row : rows) {
            if (row.getTagId() != null && row.getTagName() != null) {
                tagsMap.putIfAbsent(row.getTagId(), new Tag(row.getTagId(), row.getTagName()));
            }
            if (row.getIngredientId() != null && row.getIngredientName() != null) {
                ingredientsMap.putIfAbsent(row.getIngredientId(),
                        new IngredientDTO(row.getIngredientId(), row.getIngredientName(), row.getIngredientQuantity(), row.getIngredientMeasure()));
            }
        }

        return new RecipeDTO(
                firstRow.getRecipeId(),
                firstRow.getKeycloakId(),
                firstRow.getImageUrl(),
                firstRow.getTitle(),
                firstRow.getDescription(),
                firstRow.getContents(),
                new ArrayList<>(tagsMap.values()),
                new ArrayList<>(ingredientsMap.values())
        );
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