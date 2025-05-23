package md.ctif.recipes_app.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public final class ParagraphBlockDTO implements ContentBlockDTO {
    private String type;
    private ParagraphData data;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class ParagraphData {
        private String text;
    }
}
