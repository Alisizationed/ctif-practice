package md.ctif.recipes_app.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
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
}