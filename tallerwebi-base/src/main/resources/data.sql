-- CREATE DATABASE IF NOT EXISTS tallerwebi;
-- USE tallerwebi;

-- INICIO DE LA ZONA CRÍTICA: Deshabilitar FKs para inserciones masivas
-- Esto ayuda a prevenir errores de orden en los scripts de inicialización
SET FOREIGN_KEY_CHECKS = 0;

-- 1️⃣ DEPARTAMENTOS Y CARRERAS
INSERT INTO Departamento(nombre) VALUES
('Ingeniería'), -- ID 1
('Matemática'), -- ID 2
('Economía'),   -- ID 3
('Gestión');    -- ID 4

INSERT INTO Carrera(nombre,departamento_id) VALUES
('Tecnicatura en Desarrollo Web',1),
('Licenciatura en Sistemas de Información',1),
('Profesorado en Matemática',2),
('Tecnicatura en Gestión de Empresas',4),
('Licenciatura en Economía',3);

-- 2️⃣ MATERIAS
INSERT INTO Materia(nombre) VALUES
('Programación I'),
('Programación II'),
('Matemática I'),
('Economía General');

-- 3️⃣ VINCULAR MATERIAS CON CARRERAS
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES
(1, 1), (1, 2), (2, 1), (2, 2), (3, 3), (4, 4);

-- 4️ CREAR USUARIOS (Incluye departamento_id y esBot=FALSE)
INSERT INTO Usuario (email, password, rol, activo, nombre, apellido, dni, carrera_id, departamento_id, fechaNacimiento, confirmado, esBot) VALUES
('test@unlam.edu.ar', 'test', 'ADMIN', true, 'Juan', 'Perez',38065944, 1, 1, '1990-01-01', true, FALSE),
('fran@unlam.edu.ar', '123', 'ADMIN', true, 'Franco', 'Vargas',41062869, 1, 1, '1998-04-24', true, FALSE),
('nat@unlam.edu.ar', '123', 'ADMIN', true, 'Nat', 'alia',41123869, 1, 1, '1998-08-29', true, FALSE),
('ro@unlam.edu.ar', '1234', 'ADMIN', true, 'Ro', 'Campa',37659747, 1, 1, '1993-06-18', true, FALSE);

-- 🤖 USUARIOS BOT (ASIGNADOS A IDs SUPERIORES)
-- Asumiremos que los IDs 1-5 son humanos. Los bots serán 6, 7, 8.

-- Bot 1: Publicidad General (Carrera 2: Sistemas)
INSERT INTO Usuario(email, password, rol, activo, nombre, apellido, dni, carrera_id, departamento_id, fechaNacimiento, confirmado, esBot)
VALUES ('unibot@unlam.edu.ar', 'botpass', 'BOT', true, 'UNLaM', 'Informa', 99990001, 2, 1, '2025-01-01', true, TRUE);

-- Bot 2: Departamento de Alumnos (Carrera 4: Gestión)
INSERT INTO Usuario(email, password, rol, activo, nombre, apellido, dni, carrera_id, departamento_id, fechaNacimiento, confirmado, esBot)
VALUES ('alumnobot@unlam.edu.ar', 'botpass', 'BOT', true, 'Depto', 'Alumnos', 99990002, 4, 4, '2025-01-01', true, TRUE);

-- Bot 3: Facultad/Departamento Temático (Carrera 1: Desarrollo Web)
INSERT INTO Usuario(email, password, rol, activo, nombre, apellido, dni, carrera_id, departamento_id, fechaNacimiento, confirmado, esBot)
VALUES ('ingenieriabot@unlam.edu.ar', 'botpass', 'BOT', true, 'Ingeniería', 'Tech', 99990003, 1, 1, '2025-01-01', true, TRUE);

INSERT INTO Usuario(email, password, rol, activo, nombre, apellido, dni, carrera_id, departamento_id, fechaNacimiento, confirmado, esBot)
VALUES ('admin@unlam.edu.ar', 'admin', 'USER', true, 'Admin', 'Unlam', 32912293, 3, 2, '1990-01-01', true, FALSE);

-- 🤖 USUARIO BOT DE PRUEBA
INSERT INTO Usuario(email, password, rol, activo, nombre, apellido, dni, carrera_id, departamento_id, fechaNacimiento, confirmado, esBot)
VALUES ('bot@unlam.edu.ar', 'botpass', 'BOT', true, 'Uni', 'Bot', 99999999, 2, 1, '2025-01-01', true, TRUE); -- ⬅️ ¡BOT DEFINIDO!

-- 5️ Validar que no haya usuarios duplicados
SELECT email, COUNT(*) FROM Usuario GROUP BY email HAVING COUNT(*) > 1;

-- Géneros
INSERT INTO genero (nombre) VALUES ('Femenino'), ('Masculino'), ('Otro'), ('Prefiero no decirlo');

-- **PUBLICACIONES BASE Y TEMÁTICAS**
ALTER TABLE Publicacion
CONVERT TO CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

-- ID 1
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad)
VALUES ('Publicación vieja de prueba', '2025-09-27 10:00:00', 3, false);
UPDATE Usuario
SET ultima_publicacion = '2025-09-27'
WHERE id = 3;

-- IDs 2 a 8 (Sentencias separadas para evitar errores)
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad) VALUES ('Explorando la programación avanzada', '2025-10-01 09:00:00', 1, false);
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad) VALUES ('Mis apuntes de matemática I', '2025-10-02 14:30:00', 2, false);
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad) VALUES ('Economía general: conceptos clave', '2025-10-03 11:15:00', 4, false);
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad) VALUES ('Proyecto final de desarrollo web', '2025-10-04 16:45:00', 1, false);
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad) VALUES ('Inteligencia artificial aplicada a la cocina', '2025-10-08 09:00:00', 1,  false);
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad) VALUES ('Cómo diseñar experiencias gastronómicas inmersivas', '2025-10-08 17:20:00', 2, false);

INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad) VALUES ('El futuro de la sostenibilidad alimentaria', '2025-10-09 13:15:00', 4, false);
-- ID 9
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad)
VALUES ('Análisis profundo de estructuras de datos y algoritmos avanzados en Java.', NOW(), 1, false);

-- ID 10
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad)
VALUES ('El impacto de la inflación en la toma de decisiones de inversión personal.', NOW(), 4, false);

-- ID 11
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad)
VALUES ('Introducción a la criptografía aplicada y la seguridad de la información en redes.', NOW(), 2, false);

-- ID 12
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad)
VALUES ('Guía completa sobre el framework Spring Boot y su arquitectura de microservicios.', NOW(), 1, false);

-- ID 13
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad)
VALUES ('Conceptos clave de cálculo multivariable para el análisis de sistemas dinámicos.', NOW(), 2, false);

-- ID 14
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad)
VALUES ('Diseño de bases de datos NoSQL y su aplicación en proyectos de gran escala.', NOW(), 4, false);

-- ID 15
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad)
VALUES ('La economía del comportamiento: sesgos cognitivos en el mercado financiero.', NOW(), 4, false);

-- ID 16
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad)
VALUES ('Estrategias para la optimización de código y mejora del rendimiento de aplicaciones.', NOW(), 1, false);



-- 6️Generar slug para los nuevos usuarios
UPDATE Usuario
SET slug = LOWER(CONCAT(nombre, '-', apellido, '-', LPAD(id, 4, '0')))
WHERE slug IS NULL OR slug = '';

-- Validar
SELECT * FROM Interaccion WHERE usuario_id = 3;

-- 🟢 FIN DE LA ZONA CRÍTICA: Habilitar FKs nuevamente
SET FOREIGN_KEY_CHECKS = 1;