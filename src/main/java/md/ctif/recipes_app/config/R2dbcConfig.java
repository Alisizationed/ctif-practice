package md.ctif.recipes_app.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class R2dbcConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Object> converters = new ArrayList<>();
        converters.add(new JsonNodeReadConverter(objectMapper));
        converters.add(new JsonNodeWriteConverter(objectMapper));
//        converters.add(new VectorReadingConverter());
//        converters.add(new VectorWritingConverter());
        return new R2dbcCustomConversions(R2dbcCustomConversions.StoreConversions.NONE, converters);
    }

    @ReadingConverter
    public static class JsonNodeReadConverter implements Converter<Json, JsonNode> {
        private final ObjectMapper objectMapper;

        public JsonNodeReadConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public JsonNode convert(Json source) {
            try {
                return objectMapper.readTree(source.asString());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert JSONB to JsonNode", e);
            }
        }
    }

    @WritingConverter
    public static class JsonNodeWriteConverter implements Converter<JsonNode, Json> {
        private final ObjectMapper objectMapper;

        public JsonNodeWriteConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Json convert(JsonNode source) {
            try {
                return Json.of(objectMapper.writeValueAsString(source));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert JsonNode to JSONB", e);
            }
        }
    }

    @ReadingConverter
    public static class VectorReadingConverter implements Converter<String, float[]> {
        @Override
        public float[] convert(String source) {
            if (source == null || source.isBlank()) return null;
            String vectorString = source.trim();
            vectorString = vectorString.replaceAll("^\\[|\\]$", "");
            if (vectorString.isEmpty()) return new float[0];
            String[] parts = vectorString.split(",");
            float[] vector = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                vector[i] = Float.parseFloat(parts[i].trim());
            }
            return vector;
        }
    }

    @WritingConverter
    public static class VectorWritingConverter implements Converter<float[], String> {
        @Override
        public String convert(float[] source) {
            if (source == null || source.length == 0) return "[]";
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < source.length; i++) {
                sb.append(source[i]);
                if (i < source.length - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }
    }
}