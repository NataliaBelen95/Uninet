-- CREATE DATABASE IF NOT EXISTS tallerwebi;
-- USE tallerwebi;

-- INICIO DE LA ZONA CRÍTICA: Deshabilitar FKs para inserciones masivas
-- Esto ayuda a prevenir errores de orden en los scripts de inicialización
SET FOREIGN_KEY_CHECKS = 0;

--  DEPARTAMENTOS Y CARRERAS
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

-- 2⃣MATERIAS
INSERT INTO Materia(nombre) VALUES
('Programación I'),
('Programación II'),
('Matemática I'),
('Economía General');

-- 3⃣VINCULAR MATERIAS CON CARRERAS
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES
(1, 1), (1, 2), (2, 1), (2, 2), (3, 3), (4, 4);

-- 4️ CREAR USUARIOS (Incluye departamento_id y esBot=FALSE)
INSERT INTO Usuario (email, password, rol, activo, nombre, apellido, dni, carrera_id, departamento_id, fechaNacimiento, confirmado, esBot) VALUES
('test@unlam.edu.ar', 'test', 'ADMIN', true, 'Juan', 'Perez',38065944, 1, 2, '1990-01-01', true, FALSE),
('fran@unlam.edu.ar', '123', 'ADMIN', true, 'Franco', 'Vargas',41062869, 1, 1, '1998-04-24', true, FALSE),
('nat@unlam.edu.ar', '123', 'ADMIN', true, 'Nat', 'alia',41123869, 1, 1, '1998-08-29', true, FALSE),
('ro@unlam.edu.ar', '1234', 'ADMIN', true, 'Ro', 'Campa',37659747, 1, 1, '1993-06-18', true, FALSE);

-- USUARIOS BOT (ASIGNADOS A IDs SUPERIORES)
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

-- USUARIO BOT DE PRUEBA
INSERT INTO Usuario(email, password, rol, activo, nombre, apellido, dni, carrera_id, departamento_id, fechaNacimiento, confirmado, esBot)
VALUES ('bot@unlam.edu.ar', 'botpass', 'BOT', true, 'Uni', 'Bot', 99999999, 2, 1, '2025-01-01', true, TRUE); -- ⬅¡BOT DEFINIDO!

-- 5️ Validar que no haya usuarios duplicados
SELECT email, COUNT(*) FROM Usuario GROUP BY email HAVING COUNT(*) > 1;

-- Géneros
INSERT INTO genero (nombre) VALUES ('Femenino'), ('Masculino'), ('Otro'), ('Prefiero no decirlo');

-- **PUBLICACIONES BASE Y TEMÁTICAS**
ALTER TABLE Publicacion
CONVERT TO CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

-- ULTIMA PUBLICACION DE PRUEBA SCHEDUle NOTIFICACION
--INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad)
--VALUES ('Publicación vieja de prueba', '2025-09-27 10:00:00', 3, false);
--UPDATE Usuario
--SET ultima_publicacion = '2025-09-27'
--WHERE id = 3;
-- ULTIMA PUBLICACION DE PRUEBA SCHEDule NOTIFICACION


-- Contenido para Lucene (ID 1-16)
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad) VALUES
( 'Publicación vieja de prueba', '2025-09-27 10:00:00', 3, FALSE), -- ID 1
('Explorando la programación avanzada, me encanta Java y Spring.', NOW(), 1, FALSE), -- ID 2
('Mis apuntes de matemática I', '2025-10-02 14:30:00', 2, FALSE), -- ID 3
('Economía general: conceptos clave de la demanda agregada.', NOW(), 4, FALSE), -- ID 4
('Proyecto final de desarrollo web', '2025-10-04 16:45:00', 1, FALSE), -- ID 5
('Inteligencia artificial aplicada a la cocina, IA y gastronomía.', NOW(), 1, FALSE), -- ID 6
('Cómo diseñar experiencias gastronómicas inmersivas', '2025-10-08 17:20:00', 2, FALSE), -- ID 7
('El futuro de la sostenibilidad alimentaria', '2025-10-09 13:15:00', 4, FALSE), -- ID 8
('Análisis profundo de estructuras de datos y algoritmos avanzados en Java.', NOW(), 1, FALSE), -- ID 9
('El impacto de la inflación en la toma de decisiones de inversión personal.', NOW(), 4, FALSE), -- ID 10
('Introducción a la criptografía aplicada y la seguridad de la información en redes.', NOW(), 2, FALSE), -- ID 11
('Guía completa sobre el framework Spring Boot y su arquitectura de microservicios.', NOW(), 1, FALSE), -- ID 12
('Conceptos clave de cálculo multivariable para el análisis de sistemas dinámicos.', NOW(), 2, FALSE), -- ID 13
('Diseño de bases de datos NoSQL y su aplicación en proyectos de gran escala.', NOW(), 4, FALSE), -- ID 14
('La economía del comportamiento: sesgos cognitivos en el mercado financiero.', NOW(), 4, FALSE), -- ID 15
('Estrategias para la optimización de código y mejora del rendimiento de aplicaciones.', NOW(), 1, FALSE); -- ID 16


-- 9️ PUBLICACIONES DETALLADAS PARA JUAN PEREZ (ID 1)
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad) VALUES
('Primeros pasos con Docker para el deploy de mi app web. ¡Mucho que aprender!', NOW(), 1, FALSE), -- ID 17
('Mi café matutino y el roadmap para terminar el TP de Programación II. ', NOW(), 1, FALSE), -- ID 18
('¿Vale la pena aprender Angular ahora? Escucho opiniones sobre frameworks frontend.', NOW(), 1, FALSE), -- ID 19
('Diseñando la UX de mi proyecto web. La simplicidad es clave.', NOW(), 1, FALSE); -- ID 20

-- 10️ PUBLICACIONES DETALLADAS PARA NATALIA (ID 3)
INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id, esPublicidad) VALUES
('Necesito consejos para optimizar la performance de mi código Java. ¿Algún truco?', NOW(), 3, FALSE), -- ID 23
('Leyendo sobre la ética de la IA en el desarrollo de software. Tema importante.', NOW(), 3, FALSE), -- ID 24
('Mi resumen para el final de Sistemas Operativos. ¡Espero aprobar!', NOW(), 3, FALSE), -- ID 25
('El dilema de elegir entre Python y Javascript para scripting. ¿Cuál prefieren?', NOW(), 3, FALSE), -- ID 26
('Probando nuevos temas oscuros para mi IDE. Visual Studio Code tiene los mejores.', NOW(), 3, FALSE); -- ID 27

INSERT INTO SolicitudAmistad (solicitante_id, receptor_id, estado, fechaSolicitud)
VALUES (2, 3, 'ACEPTADA', NOW());
INSERT INTO Amistad (solicitante_id, solicitado_id)
VALUES (2, 3);

-- 6️Generar slug para los nuevos usuarios
UPDATE Usuario
SET slug = LOWER(CONCAT(nombre, '-', apellido, '-', LPAD(id, 4, '0')))
WHERE slug IS NULL OR slug = '';

-- Validar
SELECT * FROM Interaccion WHERE usuario_id = 3;

-- 🟢 FIN DE LA ZONA CRÍTICA: Habilitar FKs nuevamente
SET FOREIGN_KEY_CHECKS = 1;