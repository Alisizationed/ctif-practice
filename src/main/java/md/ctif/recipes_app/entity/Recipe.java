package md.ctif.recipes_app.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.ctif.recipes_app.DTO.RecipeDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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
    private String keycloakId;
    private String title;
    private String description;
    private String image;
    private JsonNode contents;
    @Transient
    private List<Tag> tags;
    @Transient
    private List<Ingredient> ingredients;
    public Recipe(RecipeDTO recipeDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        this.keycloakId = recipeDTO.keycloakId();
        this.title = recipeDTO.title();
        this.description = recipeDTO.description();
        try {
            this.contents = objectMapper.readTree(recipeDTO.contents());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        this.image = recipeDTO.image();
    }
    public void update(RecipeDTO recipeDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        this.title = recipeDTO.title();
        this.description = recipeDTO.description();
        if(recipeDTO.image() != null && !recipeDTO.image().isBlank()) {
            this.image = recipeDTO.image();
        }
        try {
            this.contents = objectMapper.readTree(recipeDTO.contents());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
