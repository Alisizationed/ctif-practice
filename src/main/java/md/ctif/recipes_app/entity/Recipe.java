package md.ctif.recipes_app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table
public class Recipe {
    @Id
    private Long id;
    private Long userProfileId;
    private String title;
    private String description;
    private String image;
    @Transient
    private List<ContentBlock> contentBlocks;
    @Transient
    private List<Tag> tags;
    @Transient
    private List<Ingredient> ingredients;
}
