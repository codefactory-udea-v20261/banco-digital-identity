CREATE TABLE permiso (
    id SMALLSERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE rol_permiso (
    rol_id SMALLINT NOT NULL REFERENCES rol(id) ON DELETE CASCADE,
    permiso_id SMALLINT NOT NULL REFERENCES permiso(id) ON DELETE CASCADE,
    PRIMARY KEY (rol_id, permiso_id)
);

-- Insert Basic Permissions
INSERT INTO permiso (id, nombre, descripcion) VALUES
(1, 'PERM_MANAGE_CLIENTS', 'Permite crear y modificar perfiles de clientes'),
(2, 'PERM_CREATE_ACCOUNTS', 'Permite abrir nuevas cuentas financieras para clientes'),
(3, 'PERM_VIEW_AUDIT', 'Permite consultar los registros de auditoria del sistema'),
(4, 'PERM_GENERATE_REPORTS', 'Permite generar reportes financieros y consolidados de cualquier cuenta'),
(5, 'PERM_READ_OWN_PROFILE', 'Permite al cliente leer su propio perfil'),
(6, 'PERM_READ_OWN_BALANCE', 'Permite al cliente consultar el saldo de sus propias cuentas'),
(7, 'PERM_TRANSACT_OWN_ACCOUNTS', 'Permite al cliente realizar transacciones desde sus propias cuentas'),
(8, 'PERM_GENERATE_OWN_REPORTS', 'Permite al cliente generar reportes de sus propias cuentas');

-- Assing Permissions to Roles
-- ADMIN (id: 1) gets all permissions
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8);

-- CAJERO (id: 2)
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
(2, 1), (2, 2);

-- CLIENTE (id: 3)
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
(3, 5), (3, 6), (3, 7), (3, 8);

-- AUDITOR (id: 4)
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
(4, 3), (4, 4);

-- Update Sequence for Permiso (just in case more are added)
SELECT setval('permiso_id_seq', (SELECT MAX(id) FROM permiso));