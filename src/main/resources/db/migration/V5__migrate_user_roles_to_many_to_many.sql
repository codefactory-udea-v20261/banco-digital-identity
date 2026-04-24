-- ════════════════════════════════════════════════════════════
-- V5: Migración de usuario-rol a relación muchos a muchos
-- Módulo: auth
-- ════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS usuario_rol (
    usuario_id UUID     NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    rol_id     SMALLINT NOT NULL REFERENCES rol(id),
    PRIMARY KEY (usuario_id, rol_id)
);

INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT id, rol_id
FROM usuario
WHERE rol_id IS NOT NULL
ON CONFLICT DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_usuario_rol_rol_id ON usuario_rol (rol_id);

ALTER TABLE usuario DROP COLUMN IF EXISTS rol_id;

GRANT SELECT, INSERT, DELETE ON usuario_rol TO app_user;
