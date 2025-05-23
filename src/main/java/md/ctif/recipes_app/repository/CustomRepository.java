package md.ctif.recipes_app.repository;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.ctif.recipes_app.DTO.*;
import md.ctif.recipes_app.entity.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class CustomRepository {
    @Autowired
    private DatabaseClient client;

    public Mono<RecipeDTO> fetchRecipeDetails(Long recipeId) {
        Flux<FlatRecipeRow> flatRows = client.sql("""
                            SELECT 
                              r.id AS r_id, r.user_profile_id, r.title, r.description, r.image AS r_image,
                              cb.id AS cb_id, cb.type AS cb_type, cb.text AS cb_text, cb.url AS cb_url, cb.position AS cb_position,
                              t.id AS tag_id, t.tag AS tag_name,
                              i.id AS ing_id, i.ingredient AS ing_name, ri.amount AS ing_quantity, ri.measure AS ing_measure
                            FROM recipe r
                            LEFT JOIN content_block cb ON cb.recipe_id = r.id
                            LEFT JOIN recipe_tag rt ON rt.recipe_id = r.id
                            LEFT JOIN tag t ON t.id = rt.tag_id
                            LEFT JOIN recipe_ingredient ri ON ri.recipe_id = r.id
                            LEFT JOIN ingredient i ON i.id = ri.ingredient_id
                            WHERE r.id = $1
                            ORDER BY cb_position ASC
                        """)
                .bind(0, recipeId)
                .map((row, meta) -> new FlatRecipeRow(
                        row.get("r_id", Long.class),
                        row.get("user_profile_id", Long.class),
                        row.get("title", String.class),
                        row.get("description", String.class),
                        row.get("r_image", String.class),
                        row.get("cb_id", Long.class),
                        row.get("cb_type", String.class),
                        row.get("cb_text", String.class),
                        row.get("cb_url", String.class),
                        row.get("cb_position", Long.class),
                        row.get("tag_id", Long.class),
                        row.get("tag_name", String.class),
                        row.get("ing_id", Long.class),
                        row.get("ing_name", String.class),
                        row.get("ing_quantity", Long.class),
                        row.get("ing_measure", String.class)
                ))
                .all();

        Mono<RecipeDTO> recipeMono = flatRows.collectList()
                .map(rows -> {
                    if (rows.isEmpty()) return null;

                    FlatRecipeRow first = rows.get(0);

                    Map<Long, ContentBlockDTO> contentBlocks = new LinkedHashMap<>();
                    Map<Long, Tag> tags = new LinkedHashMap<>();
                    Map<Long, IngredientDTO> ingredients = new LinkedHashMap<>();

                    for (FlatRecipeRow row : rows) {
                        if (row.getContentBlockId() != null && !contentBlocks.containsKey(row.getContentBlockId())) {
                            contentBlocks.put(row.getContentBlockPosition(),
                                    (row.getContentBlockType().equals("image")) ?
                                            new ImageBlockDTO(row.getContentBlockType(),
                                                    new ImageBlockDTO.ImageData(
                                                            new ImageBlockDTO.ImageData.File(row.getContentBlockUrl()
                                                            )))
                                            :
                                            new ParagraphBlockDTO(row.getContentBlockType(),
                                                    new ParagraphBlockDTO.ParagraphData(row.getContentBlockText()
                                                    )));
                        }
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
                            first.getUserProfileId(),
                            first.getImageUrl(),
                            first.getTitle(),
                            first.getDescription(),
                            new ArrayList<>(contentBlocks.values()),
                            new ArrayList<>(tags.values()),
                            new ArrayList<>(ingredients.values())
                    );
                });
        return recipeMono;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private class FlatRecipeRow {
        private Long recipeId;
        private Long userProfileId;
        private String title;
        private String description;
        private String imageUrl;

        private Long contentBlockId;
        private String contentBlockType;
        private String contentBlockText;
        private String contentBlockUrl;
        private Long contentBlockPosition;

        private Long tagId;
        private String tagName;

        private Long ingredientId;
        private String ingredientName;
        private Long ingredientQuantity;
        private String ingredientMeasure;
    }

    public Flux<RecipeDTO> getAllRecipes() {
        Flux<FlatRecipeRow> flatRows = client.sql("""
                            SELECT 
                              r.id AS r_id, r.user_profile_id, r.title, r.description, r.image AS r_image,
                              cb.id AS cb_id, cb.type AS cb_type, cb.text AS cb_text, cb.url AS cb_url, cb.position AS cb_position,
                              t.id AS tag_id, t.tag AS tag_name,
                              i.id AS ing_id, i.ingredient AS ing_name, ri.amount AS ing_quantity, ri.measure AS ing_measure
                            FROM recipe r
                            LEFT JOIN content_block cb ON cb.recipe_id = r.id
                            LEFT JOIN recipe_tag rt ON rt.recipe_id = r.id
                            LEFT JOIN tag t ON t.id = rt.tag_id
                            LEFT JOIN recipe_ingredient ri ON ri.recipe_id = r.id
                            LEFT JOIN ingredient i ON i.id = ri.ingredient_id
                            ORDER BY r_id, cb_position ASC
                        """)
                .map((row, meta) -> new FlatRecipeRow(
                        row.get("r_id", Long.class),
                        row.get("user_profile_id", Long.class),
                        row.get("title", String.class),
                        row.get("description", String.class),
                        row.get("r_image", String.class),
                        row.get("cb_id", Long.class),
                        row.get("cb_type", String.class),
                        row.get("cb_text", String.class),
                        row.get("cb_url", String.class),
                        row.get("cb_position", Long.class),
                        row.get("tag_id", Long.class),
                        row.get("tag_name", String.class),
                        row.get("ing_id", Long.class),
                        row.get("ing_name", String.class),
                        row.get("ing_quantity", Long.class),
                        row.get("ing_measure", String.class)
                ))
                .all();

        Flux<RecipeDTO> recipeFlux = flatRows
                .groupBy(FlatRecipeRow::getRecipeId)
                .flatMap(group -> group.collectList().map(rows -> {
                    if (rows.isEmpty()) return null;

                    FlatRecipeRow first = rows.get(0);

                    Map<Long, ContentBlockDTO> contentBlocks = new LinkedHashMap<>();
                    Map<Long, Tag> tags = new LinkedHashMap<>();
                    Map<Long, IngredientDTO> ingredients = new LinkedHashMap<>();

                    for (FlatRecipeRow row : rows) {
                        if (row.getContentBlockId() != null && !contentBlocks.containsKey(row.getContentBlockId())) {
                            contentBlocks.put(row.getContentBlockPosition(),
                                    "image".equals(row.getContentBlockType())
                                            ? new ImageBlockDTO(row.getContentBlockType(),
                                            new ImageBlockDTO.ImageData(
                                                    new ImageBlockDTO.ImageData.File(row.getContentBlockUrl())))
                                            : new ParagraphBlockDTO(row.getContentBlockType(),
                                            new ParagraphBlockDTO.ParagraphData(row.getContentBlockText())));
                        }
                        if (row.getTagId() != null && !tags.containsKey(row.getTagId())) {
                            tags.put(row.getTagId(), new Tag(row.getTagId(), row.getTagName()));
                        }
                        if (row.getIngredientId() != null && !ingredients.containsKey(row.getIngredientId())) {
                            ingredients.put(row.getIngredientId(),
                                    new IngredientDTO(row.getIngredientId(), row.getIngredientName(),
                                            row.getIngredientQuantity(), row.getIngredientMeasure()));
                        }
                    }

                    return new RecipeDTO(
                            first.getRecipeId(),
                            first.getUserProfileId(),
                            first.getImageUrl(),
                            first.getTitle(),
                            first.getDescription(),
                            new ArrayList<>(contentBlocks.values()),
                            new ArrayList<>(tags.values()),
                            new ArrayList<>(ingredients.values())
                    );
                }));
        return recipeFlux;
    }
}
