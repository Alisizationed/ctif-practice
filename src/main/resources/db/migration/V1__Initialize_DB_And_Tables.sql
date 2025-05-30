CREATE TABLE user_profile(
    id BIGSERIAL PRIMARY KEY,
    keycloak_id BIGINT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    profile_picture TEXT NOT NULL,
    bio TEXT NOT NULL
);

CREATE TABLE recipe(
   id BIGSERIAL PRIMARY KEY,
   user_profile_id BIGINT REFERENCES user_profile(id),
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

-- CREATE TABLE content_block(
--    id BIGSERIAL PRIMARY KEY,
--    recipe_id BIGINT REFERENCES recipe(id),
--    type TEXT NOT NULL,
--    text TEXT,
--    url TEXT,
--    position INT NOT NULL
-- );

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