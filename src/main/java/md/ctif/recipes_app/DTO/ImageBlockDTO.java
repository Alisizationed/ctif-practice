package md.ctif.recipes_app.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public final class ImageBlockDTO implements ContentBlockDTO {
    private String type;
    private ImageData data;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class ImageData {
        private File file;

        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        @Builder
        public static class File {
            private String url;
        }
    }
}
