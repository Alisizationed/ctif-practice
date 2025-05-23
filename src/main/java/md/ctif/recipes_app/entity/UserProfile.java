package md.ctif.recipes_app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table("user_profile")
public class UserProfile {
    @Id
    private Long id;
    private Long keycloakId;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private String bio;
}
