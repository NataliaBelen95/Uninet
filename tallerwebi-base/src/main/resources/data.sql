--DROP DATABASE tallerwebi;
--CREATE DATABASE tallerwebi;

INSERT IGNORE INTO Carrera(nombre) VALUES
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
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, carrera_id)
VALUES (NULL, 'test@unlam.edu.ar', 'test', 'ADMIN', true, 'Juan', 'Perez', 1);
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, carrera_id)
VALUES (NULL, 'admin@unlam.edu.ar', 'admin', 'USER', true, 'Admin', 'Unlam', 3);

-- 5️⃣ Validar que no haya usuarios duplicados
SELECT email, COUNT(*) FROM Usuario GROUP BY email HAVING COUNT(*) > 1;
