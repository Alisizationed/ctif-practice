package md.ctif.recipes_app.repository;

import io.r2dbc.spi.Row;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.ctif.recipes_app.DTO.ShortRecipeDTO;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@AllArgsConstructor
@Repository
public class ShortRecipeRepository {
    private final DatabaseClient client;
    private final static String GET_RECIPES_SQL = """
                    SELECT 
                      r.id AS r_id, r.created_by, r.title, r.image AS r_image
                    FROM recipe r
                    LEFT JOIN recipe_tag rt ON rt.recipe_id = r.id
                    LEFT JOIN tag t ON t.id = rt.tag_id
                    LEFT JOIN recipe_ingredient ri ON ri.recipe_id = r.id
                    LEFT JOIN ingredient i ON i.id = ri.ingredient_id
                """;;

    public Flux<ShortRecipeDTO> getAllRecipesShort() {
        Flux<FlatShortRecipeRow> flatRows = getFlatShortRecipeRowFlux();

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

    private Flux<FlatShortRecipeRow> getFlatShortRecipeRowFluxByUser(String id) {
        return client.sql(GET_RECIPES_SQL + "WHERE r.created_by = $1")
                .bind(0, id)
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

