--CREATE DATABASE IF NOT EXISTS tallerwebi;
USE tallerwebi;

UPDATE Usuario
SET slug = LOWER(CONCAT(nombre, '-', apellido, '-', LPAD(id, 4, '0')))
WHERE slug IS NULL OR slug = '';

INSERT INTO Carrera(nombre) VALUES
('Tecnicatura en Desarrollo Web'),
('Licenciatura en Sistemas de Información'),
('Profesorado en Matemática'),
('Tecnicatura en Gestión de Empresas'),
('Licenciatura en Economía');


-- 2️⃣ Crear materias (deja que el ID se autoasigne)
INSERT INTO Materia(id, nombre) VALUES (NULL, 'Programación I');
INSERT INTO Materia(id, nombre) VALUES (NULL, 'Programación II');
INSERT INTO Materia(id, nombre) VALUES (NULL, 'Matemática I');
INSERT INTO Materia(id, nombre) VALUES (NULL, 'Economía General');

-- Supongamos que las materias quedaron con IDs del 1 al 4 (verificalo con SELECT * FROM Materia;)

-- 3️⃣ Vincular materias con carreras usando los IDs de carrera que me diste:
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (1, 1); -- Tecnicatura en Desarrollo Web - Programación I
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (1, 2); -- Tecnicatura en Desarrollo Web - Programación II
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (2, 1); -- Licenciatura en Sistemas de Información - Programación I
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (2, 2); -- Licenciatura en Sistemas de Información - Programación II
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (3, 3); -- Profesorado en Matemática - Matemática I
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (4, 4); -- Tecnicatura en Gestión de Empresas - Economía General

-- 4️⃣ Crear usuarios y asignarles carrera por id
INSERT INTO Usuario (id, email, password, rol, activo, nombre, apellido, dni, carrera_id, fechaNacimiento, confirmado)
VALUES (NULL, 'test@unlam.edu.ar', 'test', 'ADMIN', true, 'Juan', 'Perez',38065944, 1, '1990-01-01', true);
INSERT INTO Usuario (id, email, password, rol, activo, nombre, apellido, dni, carrera_id, fechaNacimiento, confirmado)
VALUES (NULL, 'fran@unlam.edu.ar', '123', 'ADMIN', true, 'Franco', 'Vargas',41062869, 1, '1998-04-24', true);
INSERT INTO Usuario (id, email, password, rol, activo, nombre, apellido, dni, carrera_id, fechaNacimiento, confirmado)
VALUES (NULL, 'nat@unlam.edu.ar', '123', 'ADMIN', true, 'Nat', 'alia',41123869, 1, '1998-08-29', true);

INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, dni, carrera_id, fechaNacimiento, confirmado)
VALUES (NULL, 'admin@unlam.edu.ar', 'admin', 'USER', true, 'Admin', 'Unlam', 32912293, 3, '1990-01-01', true);



-- 5️⃣ Validar que no haya usuarios duplicados
SELECT email, COUNT(*) FROM Usuario GROUP BY email HAVING COUNT(*) > 1;

-- Géneros
INSERT INTO genero (nombre) VALUES ('Femenino'), ('Masculino'), ('Otro'), ('Prefiero no decirlo');

ALTER TABLE Publicacion
CONVERT TO CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

-- 6️⃣ Generar slug para los nuevos usuarios
UPDATE Usuario
SET slug = LOWER(CONCAT(nombre, '-', apellido, '-', LPAD(id, 4, '0')))
WHERE slug IS NULL OR slug = '';