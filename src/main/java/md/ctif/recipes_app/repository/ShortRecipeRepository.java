package md.ctif.recipes_app.repository;

import io.r2dbc.spi.Row;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.ctif.recipes_app.DTO.ShortRecipeDTO;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Repository
public class ShortRecipeRepository {
    private final static String GET_RECIPES_SQL = """
                SELECT 
                  r.id AS r_id, r.created_by, r.title, r.image AS r_image
                FROM recipe r
            """;
    private static final String GET_RECIPE_EMBEDDING_SQL = """
                SELECT
                  r.id AS r_id, r.created_by, r.title, r.image AS r_image,
                  e.embedding
                FROM recipe r
                LEFT JOIN embeddings e ON r.id = e.recipe_id
            """;
    private static final String SELECT_EMBEDDING_SQL = """
            SELECT embedding::float4[] AS embedding
            FROM embeddings
            WHERE recipe_id = :recipeId
            """;
    private final DatabaseClient client;

    public Flux<ShortRecipeDTO> getAllRecipesShort() {
        Flux<FlatShortRecipeRow> flatRows = getFlatShortRecipeRowFlux();
        return flatRows
                .groupBy(FlatShortRecipeRow::getRecipeId)
                .flatMap(group -> group.collectList()
                        .mapNotNull(this::getShortRecipeDTO));
    }

    public Flux<ShortRecipeDTO> getAllRecipesShortPageable(Long offset, Long limit) {
        Flux<FlatShortRecipeRow> flatRows = getFlatShortRecipeRowFluxPageable(limit, limit);
        return flatRows
                .groupBy(FlatShortRecipeRow::getRecipeId)
                .flatMap(group -> group.collectList()
                        .mapNotNull(this::getShortRecipeDTO));
    }

    private ShortRecipeDTO getShortRecipeDTO(List<FlatShortRecipeRow> rows) {
        if (rows.isEmpty()) return null;
        FlatShortRecipeRow first = rows.getFirst();
        return new ShortRecipeDTO(
                first.getRecipeId(),
                first.getKeycloakId(),
                first.getImageUrl(),
                first.getTitle()
        );
    }

    private FlatShortRecipeRow getFlatShortRecipeRow(Row row) {
        return new FlatShortRecipeRow(
                row.get("r_id", Long.class),
                row.get("created_by", String.class),
                row.get("title", String.class),
                row.get("r_image", String.class)
        );
    }

    private Flux<FlatShortRecipeRow> getFlatShortRecipeRowFlux() {
        return client.sql(GET_RECIPES_SQL)
                .map((row, meta) -> getFlatShortRecipeRow(row))
                .all();
    }

    private Flux<FlatShortRecipeRow> getFlatShortRecipeRowFluxPageable(Long offset, Long limit) {
        return client.sql(GET_RECIPES_SQL + "LIMIT :limit OFFSET :offset")
                .bind("limit", limit)
                .bind("offset", offset)
                .map((row, meta) -> getFlatShortRecipeRow(row))
                .all();
    }

    private Flux<FlatShortRecipeRow> getFlatShortRecipeRowFluxByUser(String id) {
        return client.sql(GET_RECIPES_SQL + "WHERE r.created_by = $1")
                .bind(0, id)
                .map((row, meta) -> getFlatShortRecipeRow(row))
                .all();
    }

    private Flux<FlatShortRecipeRow> getFlatShortRecipeRowFluxByUserPageable(String id, Long offset, Long limit) {
        return client.sql(GET_RECIPES_SQL + "WHERE r.created_by = $1 LIMIT $2 OFFSET $3")
                .bind(0, id)
                .bind(1, limit)
                .bind(2, offset)
                .map((row, meta) -> getFlatShortRecipeRow(row))
                .all();
    }

    public Flux<ShortRecipeDTO> getAllRecipesShortByUser(String id) {
        Flux<FlatShortRecipeRow> flatRows = getFlatShortRecipeRowFluxByUser(id);
        return flatRows
                .groupBy(FlatShortRecipeRow::getRecipeId)
                .flatMap(group -> group.collectList()
                        .mapNotNull(this::getShortRecipeDTO));
    }

    public Flux<ShortRecipeDTO> getAllRecipesShortByUserPageable(String id, Long offset, Long limit) {
        Flux<FlatShortRecipeRow> flatRows = getFlatShortRecipeRowFluxByUserPageable(id,offset,limit);
        return flatRows
                .groupBy(FlatShortRecipeRow::getRecipeId)
                .flatMap(group -> group.collectList()
                        .mapNotNull(this::getShortRecipeDTO));
    }

    public Mono<ShortRecipeDTO> getRecipeShortById(Long id) {
        Mono<FlatShortRecipeRow> flatRows = getFlatShortRecipeRowFluxById(id);
        return getOneShortRecipeDTO(flatRows);
    }

    public Flux<ShortRecipeDTO> findSimilarRecipes(Long recipeId, Long limit) {
        return client.sql(SELECT_EMBEDDING_SQL)
                .bind("recipeId", recipeId)
                .map(row -> row.get("embedding", Float[].class))
                .one()
                .flatMapMany(targetEmbeddingFloats -> {
                    Objects.requireNonNull(targetEmbeddingFloats, "Embedding not found for recipe ID: " + recipeId);

                    String similarRecipesSql = GET_RECIPE_EMBEDDING_SQL + """
                            WHERE r.id != :recipeId
                            ORDER BY e.embedding <-> (:targetEmbedding::vector)
                            LIMIT :limit
                            """;

                    return client.sql(similarRecipesSql)
                            .bind("recipeId", recipeId)
                            .bind("targetEmbedding", targetEmbeddingFloats)
                            .bind("limit", limit)
                            .map((row, meta) -> getFlatShortRecipeRow(row))
                            .all()
                            .groupBy(FlatShortRecipeRow::getRecipeId)
                            .flatMap(group -> group.collectList().mapNotNull(this::getShortRecipeDTO));
                })
                .switchIfEmpty(Flux.empty());
    }

    private Mono<ShortRecipeDTO> getOneShortRecipeDTO(Mono<FlatShortRecipeRow> flatRows) {
        return flatRows.map(first -> new ShortRecipeDTO(
                first.getRecipeId(),
                first.getKeycloakId(),
                first.getImageUrl(),
                first.getTitle()
        ));
    }

    private Mono<FlatShortRecipeRow> getFlatShortRecipeRowFluxById(Long id) {
        return client.sql(GET_RECIPES_SQL + "WHERE r.id = $1")
                .bind(0, id)
                .map((row, meta) -> getFlatShortRecipeRow(row))
                .first();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class FlatShortRecipeRow {
        private Long recipeId;
        private String keycloakId;
        private String title;
        private String imageUrl;
    }
}

