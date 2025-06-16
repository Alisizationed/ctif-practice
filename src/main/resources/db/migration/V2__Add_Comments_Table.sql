CREATE TABLE comments
(
    id                BIGSERIAL PRIMARY KEY,
    content           TEXT         NOT NULL,
    recipe_id         BIGINT       NOT NULL,
    CONSTRAINT fk_comment_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id) ON DELETE CASCADE,
    parent_comment_id BIGINT,
    CONSTRAINT fk_parent_comment FOREIGN KEY (parent_comment_id) REFERENCES comments (id) ON DELETE CASCADE,
    created_at        timestamp,
    created_by        VARCHAR(255) NOT NULL,
    updated_at        timestamp,
    updated_by        VARCHAR(255)
);

CREATE INDEX idx_comments_recipe_id ON comments (recipe_id);
CREATE INDEX idx_comments_keycloak_id ON comments (created_by);
CREATE INDEX idx_comments_parent_comment_id ON comments (parent_comment_id);
CREATE INDEX idx_comments_created_at ON comments (created_at DESC);

CREATE TABLE comment_reactions
(
    id            BIGSERIAL PRIMARY KEY,
    comment_id    BIGINT       NOT NULL,
    CONSTRAINT fk_reaction_comment FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE,
    reaction_type VARCHAR(20)  NOT NULL,
    created_at    timestamp,
    created_by    VARCHAR(255),
    UNIQUE (comment_id, created_by, reaction_type)
);

CREATE INDEX idx_comment_reactions_comment_id ON comment_reactions (comment_id);
CREATE INDEX idx_comment_reactions_keycloak_id ON comment_reactions (created_by);
