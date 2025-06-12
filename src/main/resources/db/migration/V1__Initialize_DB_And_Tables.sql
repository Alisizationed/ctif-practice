CREATE
EXTENSION IF NOT EXISTS vector;

CREATE TABLE recipe
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    description TEXT         NOT NULL,
    image       TEXT,
    contents    JSONB        NOT NULL,
    created_at  timestamp,
    updated_at  timestamp,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

CREATE TABLE embeddings
(
    id        BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    embedding vector(768)
);
CREATE INDEX ON embeddings USING HNSW (embedding vector_cosine_ops);

CREATE TABLE tag
(
    id  BIGSERIAL PRIMARY KEY,
    tag VARCHAR(50) NOT NULL
);

CREATE TABLE ingredient
(
    id         BIGSERIAL PRIMARY KEY,
    ingredient VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE recipe_ingredient
(
    recipe_id     BIGINT REFERENCES recipe (id),
    ingredient_id BIGINT REFERENCES ingredient (id),
    amount        INT,
    measure       TEXT
);

CREATE TABLE recipe_tag
(
    recipe_id BIGINT REFERENCES recipe (id),
    tag_id    BIGINT NOT NULL
);