package md.ctif.recipes_app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pgvector.PGvector;
import md.ctif.recipes_app.repository.EmbeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static md.ctif.recipes_app.converters.PGvectorListFloatConvertor.floatListToArray;

@Service
public class EmbeddingService {
    private final WebClient webClient;
    private final EmbeddingRepository embeddingRepository;

    public EmbeddingService(WebClient.Builder webClientBuilder, EmbeddingRepository embeddingRepository) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:11434").build();
        this.embeddingRepository = embeddingRepository;
    }

    public Mono<PGvector> generateEmbedding(String inputText) {
        return webClient.post()
                .uri("/api/embeddings")
                .bodyValue(Map.of("model", "nomic-embed-text", "prompt", inputText))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    JsonNode embeddingNode = json.get("embedding");
                    List<Float> embeddingList = StreamSupport.stream(embeddingNode.spliterator(), false)
                            .map(JsonNode::floatValue)
                            .toList();
                    float[] embeddingArray = floatListToArray(embeddingList);
                    return new PGvector(embeddingArray);
                });
    }

    public static Float[] toObjectArray(float[] primitiveArray) {
        if (primitiveArray == null) {
            return null;
        }
        return IntStream.range(0, primitiveArray.length)
                .mapToObj(i -> primitiveArray[i])
                .toArray(Float[]::new);
    }

    public Mono<Float[]> generateFloatEmbedding(String inputText) {
        return webClient.post()
                .uri("/api/embeddings")
                .bodyValue(Map.of("model", "nomic-embed-text", "prompt", inputText))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    JsonNode embeddingNode = json.get("embedding");
                    return StreamSupport.stream(embeddingNode.spliterator(), false)
                            .map(JsonNode::floatValue)
                            .toArray(Float[]::new);
                });
    }

    public Mono<Void> saveEmbedding(Long recipeId, String inputText) {
        return embeddingRepository.saveEmbedding(recipeId, generateFloatEmbedding(inputText));
    }
}

