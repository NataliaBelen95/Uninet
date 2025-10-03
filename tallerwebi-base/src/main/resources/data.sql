-- 1️⃣ Crear carreras
INSERT INTO Carrera(id, nombre) VALUES (NULL, 'Tecnicatura en Desarrollo Web');
INSERT INTO Carrera(id, nombre) VALUES (NULL, 'Licenciatura en Sistemas de Información');
INSERT INTO Carrera(id, nombre) VALUES (NULL, 'Profesorado en Matemática');
INSERT INTO Carrera(id, nombre) VALUES (NULL, 'Tecnicatura en Gestión de Empresas');
INSERT INTO Carrera(id, nombre) VALUES (NULL, 'Licenciatura en Economía');

-- 1️⃣ Crear carreras
INSERT INTO Carrera(id, nombre) VALUES (NULL, 'Tecnicatura en Desarrollo Web');
INSERT INTO Carrera(id, nombre) VALUES (NULL, 'Licenciatura en Sistemas de Información');
INSERT INTO Carrera(id, nombre) VALUES (NULL, 'Profesorado en Matemática');
INSERT INTO Carrera(id, nombre) VALUES (NULL, 'Tecnicatura en Gestión de Empresas');
INSERT INTO Carrera(id, nombre) VALUES (NULL, 'Licenciatura en Economía');

-- 2️⃣ Crear materias (ejemplo)
INSERT INTO Materia(id, nombre) VALUES (NULL, 'Programación I');
INSERT INTO Materia(id, nombre) VALUES (NULL, 'Programación II');
INSERT INTO Materia(id, nombre) VALUES (NULL, 'Matemática I');
INSERT INTO Materia(id, nombre) VALUES (NULL, 'Economía General');

-- 3️⃣ Vincular materias con carreras (ajustar IDs según AUTO_INCREMENT)
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (1, 1);
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (1, 2);
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (2, 1);
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (2, 2);
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (3, 3);
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES (4, 4);

-- 4️⃣ Crear usuario y asignarle la carrera
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, carrera_id)
VALUES (NULL, 'test@unlam.edu.ar', 'test', 'ADMIN', true, 'Juan', 'Perez', 1);
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, carrera_id)
VALUES (NULL, 'admin@unlam.edu.ar', 'admin', 'USER', true, 'Admin', 'Unlam', 3);