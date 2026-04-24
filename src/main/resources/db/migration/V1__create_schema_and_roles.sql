-- ════════════════════════════════════════════════════════════
-- V1: Esquema base y roles de BD (Principio de Menor Privilegio)
-- Autor: Estefanía Garcés (DBA Líder)
-- ════════════════════════════════════════════════════════════

-- Roles de base de datos
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'app_readonly') THEN
        CREATE ROLE app_readonly;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'app_user') THEN
        CREATE ROLE app_user;
    END IF;
END
$$;
