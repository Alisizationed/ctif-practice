package md.ctif.recipes_app.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import md.ctif.recipes_app.DTO.RecipeDTO;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table
public class Recipe {
    @Id
    private Long id;
    private String title;
    private String description;
    private String image;
    private JsonNode contents;
    @CreatedDate
    private LocalDateTime createdAt;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @LastModifiedBy
    private String updatedBy;
    public Recipe(RecipeDTO recipeDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        this.createdBy = recipeDTO.keycloakId();
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
        if (recipeDTO.image() != null && !recipeDTO.image().isBlank()) {
            this.image = recipeDTO.image();
        }
        try {
            this.contents = objectMapper.readTree(recipeDTO.contents());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
