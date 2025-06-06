CREATE TABLE recipe(
   id BIGSERIAL PRIMARY KEY,
   keycloak_id VARCHAR(255),
   title VARCHAR(150) NOT NULL,
   description TEXT NOT NULL,
   image TEXT,
   contents JSONB NOT NULL
);

CREATE TABLE tag(
   id BIGSERIAL PRIMARY KEY,
   tag VARCHAR(50) NOT NULL
);

CREATE TABLE ingredient(
   id BIGSERIAL PRIMARY KEY,
   ingredient VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE recipe_ingredient(
   recipe_id BIGINT REFERENCES recipe(id),
   ingredient_id BIGINT REFERENCES ingredient(id),
   amount INT,
   measure TEXT
);

CREATE TABLE recipe_tag(
   recipe_id BIGINT REFERENCES recipe(id),
   tag_id BIGINT NOT NULL
);