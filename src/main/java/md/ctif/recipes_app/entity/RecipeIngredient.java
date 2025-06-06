package md.ctif.recipes_app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.ctif.recipes_app.DTO.IngredientDTO;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table("recipe_ingredient")
public class RecipeIngredient {
    private Long recipeId;
    @Column("ingredient_id")
    private Long ingredientId;
    private Long amount;
    private String measure;
    public RecipeIngredient(IngredientDTO ingredientDTO, Long ingredientId, Long recipeId) {
        this.amount = ingredientDTO.amount();
        this.measure = ingredientDTO.measure();
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
    }
}
