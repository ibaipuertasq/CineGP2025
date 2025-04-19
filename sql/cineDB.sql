/* DELETE 'cineDB' database*/
DROP SCHEMA IF EXISTS cineDB;
/* DELETE USER 'spq' AT LOCAL SERVER*/
DROP USER IF EXISTS 'spq'@'localhost';

/* CREATE 'cineDB' DATABASE */
CREATE SCHEMA  cineDB;
/* CREATE THE USER 'spq' AT LOCAL SERVER WITH PASSWORD 'spq' */
CREATE USER IF NOT EXISTS 'spq'@'localhost' IDENTIFIED BY 'spq';

GRANT ALL ON cineDB.* TO 'spq'@'localhost';


USE cineDB;
SHOW TABLES;

SELECT * FROM usuario;