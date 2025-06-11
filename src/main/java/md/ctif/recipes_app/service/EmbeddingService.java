package md.ctif.recipes_app.service;

import com.fasterxml.jackson.databind.JsonNode;
import md.ctif.recipes_app.repository.EmbeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.StreamSupport;


@Service
public class EmbeddingService {
    private final WebClient webClient;
    private final EmbeddingRepository embeddingRepository;

    public EmbeddingService(WebClient.Builder webClientBuilder, EmbeddingRepository embeddingRepository) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:11434").build();
        this.embeddingRepository = embeddingRepository;
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

