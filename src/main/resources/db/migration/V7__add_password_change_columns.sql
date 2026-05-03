-- ════════════════════════════════════════════════════════════
-- V7: Agregar columnas para account lockout y cambio de contraseña
-- Módulo: auth
-- ════════════════════════════════════════════════════════════

-- Agregar columnas a la tabla usuario para mejorar control de lockout
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS bloqueado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS last_failed_at TIMESTAMP;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS failed_attempts INTEGER NOT NULL DEFAULT 0;

-- Crear índices para mejorar performance
CREATE INDEX IF NOT EXISTS idx_usuario_bloqueado ON usuario (bloqueado);
CREATE INDEX IF NOT EXISTS idx_usuario_failed_attempts ON usuario (failed_attempts);

-- Actualizar permisos
GRANT SELECT, INSERT, UPDATE ON usuario TO app_user;
