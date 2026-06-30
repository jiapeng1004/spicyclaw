CREATE SCHEMA IF NOT EXISTS spicyclaw;

CREATE TABLE IF NOT EXISTS spicyclaw.claw_session (
    id          VARCHAR(36) PRIMARY KEY,
    title       VARCHAR(256) NOT NULL DEFAULT 'New Chat',
    agent_name  VARCHAR(128) NOT NULL DEFAULT 'SpicyClaw',
    model_ref   VARCHAR(160),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE spicyclaw.claw_session ADD COLUMN IF NOT EXISTS model_ref VARCHAR(160);

CREATE TABLE IF NOT EXISTS spicyclaw.claw_message (
    id          VARCHAR(36) PRIMARY KEY,
    session_id  VARCHAR(36) NOT NULL REFERENCES spicyclaw.claw_session(id) ON DELETE CASCADE,
    role        VARCHAR(32) NOT NULL,
    content     TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_claw_message_session ON spicyclaw.claw_message(session_id, created_at);

CREATE TABLE IF NOT EXISTS spicyclaw.claw_skill (
    id          VARCHAR(36) PRIMARY KEY,
    slug        VARCHAR(128) NOT NULL UNIQUE,
    name        VARCHAR(256) NOT NULL,
    description TEXT NOT NULL,
    source      VARCHAR(64) NOT NULL DEFAULT 'local',
    path        TEXT NOT NULL,
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    metadata    TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_claw_skill_enabled ON spicyclaw.claw_skill(enabled);

CREATE TABLE IF NOT EXISTS spicyclaw.claw_llm_model (
    id               VARCHAR(36) PRIMARY KEY,
    slug             VARCHAR(128) NOT NULL UNIQUE,
    display_name     VARCHAR(256) NOT NULL,
    provider         VARCHAR(64) NOT NULL,
    model_name       VARCHAR(128) NOT NULL,
    api_key          TEXT NOT NULL,
    base_url         TEXT,
    stream           BOOLEAN NOT NULL DEFAULT TRUE,
    enable_thinking  BOOLEAN,
    extra_options    TEXT,
    is_default       BOOLEAN NOT NULL DEFAULT FALSE,
    enabled          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_claw_llm_model_default ON spicyclaw.claw_llm_model(is_default) WHERE is_default = TRUE;
CREATE INDEX IF NOT EXISTS idx_claw_llm_model_enabled ON spicyclaw.claw_llm_model(enabled);

CREATE TABLE IF NOT EXISTS spicyclaw.claw_user (
    id              VARCHAR(36) PRIMARY KEY,
    username        VARCHAR(64) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(128),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_claw_user_username ON spicyclaw.claw_user(username);
