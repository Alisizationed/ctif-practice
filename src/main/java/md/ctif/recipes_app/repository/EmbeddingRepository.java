package md.ctif.recipes_app.repository;

import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Repository
public class EmbeddingRepository {
    private static final String UPDATE_EMBEDDINGS_SQL = """
            UPDATE embeddings SET embedding = $1 WHERE recipe_id = $2;
            """;
    private static final String INSERT_EMBEDDINGS_SQL = """
            INSERT INTO embeddings (recipe_id, embedding) VALUES ($1, ($2::vector))
            """;
    private final DatabaseClient client;

    public Mono<Void> saveEmbedding(Long id, Mono<Float[]> embeddingMono) {
        return embeddingMono.flatMap(embeddingArray ->
                client.sql(INSERT_EMBEDDINGS_SQL)
                        .bind("$1", id)
                        .bind("$2", embeddingArray)
                        .then()
        );
    }

    public Mono<Void> updateEmbedding(Long id, Mono<Float[]> embeddingMono) {
        return embeddingMono.flatMap(embeddingArray ->
                client.sql(UPDATE_EMBEDDINGS_SQL)
                        .bind("$1", embeddingArray)
                        .bind("$2", id)
                        .then()
        );
    }
}