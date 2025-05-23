package md.ctif.recipes_app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table
public class ContentBlock {
    @Id
    private Long id;
    private Long recipeId;
    private String type;
    private String text;
    private String url;
    private Long position;
}
