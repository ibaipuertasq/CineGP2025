-- Eliminar base de datos y usuario si existen
DROP SCHEMA IF EXISTS cineDB;
DROP USER IF EXISTS 'spq'@'localhost';

-- Crear la base de datos
CREATE SCHEMA cineDB;

-- Crear el usuario 'spq' con la contraseña 'spq'
CREATE USER 'spq'@'localhost' IDENTIFIED BY 'spq';

-- Otorgar privilegios al usuario 'spq' sobre la base de datos 'cineDB'
GRANT ALL PRIVILEGES ON cineDB.* TO 'spq'@'localhost';

-- Asegurarse de que los cambios se apliquen
FLUSH PRIVILEGES;
