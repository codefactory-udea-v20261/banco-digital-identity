-- ════════════════════════════════════════════════════════════
-- V8: Historial de contraseñas para prevenir reutilización
-- Módulo: auth
-- ════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS usuario_password_historial (
    id              BIGSERIAL       PRIMARY KEY,
    usuario_id      UUID            NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    password_hash   VARCHAR(255)    NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE usuario_password_historial IS
    'Almacena las últimas contraseñas usadas por el usuario para impedir su reutilización';

CREATE INDEX IF NOT EXISTS idx_password_historial_usuario
    ON usuario_password_historial (usuario_id, created_at DESC);

GRANT SELECT, INSERT, DELETE ON usuario_password_historial TO app_user;
GRANT USAGE, SELECT ON SEQUENCE usuario_password_historial_id_seq TO app_user;
