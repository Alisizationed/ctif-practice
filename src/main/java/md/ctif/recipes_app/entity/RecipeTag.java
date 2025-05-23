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
@Table("recipe_tag")
public class RecipeTag {
    @Column("recipe_id")
    private Long recipeId;
    @Column("tag_id")
    private Long tagId;
}
