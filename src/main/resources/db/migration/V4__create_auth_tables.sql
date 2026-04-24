-- ════════════════════════════════════════════════════════════
-- V4: Tablas de autenticación y seguridad
-- Módulo: auth
-- ════════════════════════════════════════════════════════════

-- ── Tabla: rol ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rol (
    id      SMALLINT    PRIMARY KEY,
    nombre  VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO rol (id, nombre) VALUES
    (1, 'ADMIN'),
    (2, 'CAJERO'),
    (3, 'CLIENTE'),
    (4, 'AUDITOR')
ON CONFLICT DO NOTHING;

-- ── Tabla: usuario ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS usuario (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    username            VARCHAR(100) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    cliente_id          UUID,
    rol_id              SMALLINT    NOT NULL REFERENCES rol(id),
    activo              BOOLEAN     NOT NULL DEFAULT TRUE,
    intentos_fallidos   SMALLINT    NOT NULL DEFAULT 0,
    bloqueado_hasta     TIMESTAMPTZ,
    ultimo_login        TIMESTAMPTZ,
    mfa_secret          VARCHAR(100),
    mfa_activo          BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON COLUMN usuario.password_hash IS 'BCrypt hash factor 12 — nunca texto plano';
COMMENT ON COLUMN usuario.mfa_secret IS 'TOTP secret para MFA';

CREATE INDEX IF NOT EXISTS idx_usuario_username ON usuario (username);
CREATE INDEX IF NOT EXISTS idx_usuario_cliente  ON usuario (cliente_id);

-- ── Tabla: token_revocado ────────────────────────────────────
CREATE TABLE IF NOT EXISTS token_revocado (
    id          BIGSERIAL   PRIMARY KEY,
    jti         VARCHAR(100) NOT NULL UNIQUE,
    usuario_id  UUID        NOT NULL REFERENCES usuario(id),
    revocado_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expira_at   TIMESTAMPTZ NOT NULL
);

COMMENT ON TABLE token_revocado IS 'Blacklist de JWT: verificar en cada request autenticado';

CREATE INDEX IF NOT EXISTS idx_token_jti     ON token_revocado (jti);
CREATE INDEX IF NOT EXISTS idx_token_expira  ON token_revocado (expira_at);

GRANT SELECT, INSERT, UPDATE ON usuario TO app_user;
GRANT SELECT, INSERT ON token_revocado TO app_user;
GRANT USAGE, SELECT ON SEQUENCE token_revocado_id_seq TO app_user;
