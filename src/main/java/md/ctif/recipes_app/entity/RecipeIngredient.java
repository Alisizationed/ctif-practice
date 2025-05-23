package md.ctif.recipes_app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
}
