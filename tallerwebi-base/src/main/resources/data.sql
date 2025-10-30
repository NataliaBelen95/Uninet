--CREATE DATABASE IF NOT EXISTS tallerwebi;
--USE tallerwebi;



INSERT INTO Departamento(nombre) VALUES
                                     ('Ingeniería'),
                                     ('Matemática'),
                                     ('Economía'),
                                     ('Gestión');

INSERT INTO Carrera(nombre,departamento_id) VALUES
                                        ('Tecnicatura en Desarrollo Web',1),
                                        ('Licenciatura en Sistemas de Información',1),
                                        ('Profesorado en Matemática',2),
                                        ('Tecnicatura en Gestión de Empresas',4),
                                        ('Licenciatura en Economía',3);

-- 2️⃣ Crear materias (deja que el ID se autoasigne)
INSERT INTO Materia(nombre) VALUES
                                ('Programación I'),
                                ('Programación II'),
                                ('Matemática I'),
                                ('Economía General');

-- Supongamos que las materias quedaron con IDs del 1 al 4 (verificalo con SELECT * FROM Materia;)

-- 3️⃣ Vincular materias con carreras usando los IDs de carrera que me diste:
INSERT INTO carrera_materia(carrera_id, materia_id) VALUES
                                                        (1, 1), -- Tecnicatura en Desarrollo Web - Programación I
                                                        (1, 2), -- Tecnicatura en Desarrollo Web - Programación II
                                                        (2, 1), -- Licenciatura en Sistemas de Información - Programación I
                                                        (2, 2), -- Licenciatura en Sistemas de Información - Programación II
                                                        (3, 3), -- Profesorado en Matemática - Matemática I
                                                        (4, 4); -- Tecnicatura en Gestión de Empresas - Economía General

-- 4️ Crear usuarios y asignarles carrera por id
INSERT INTO Usuario (email, password, rol, activo, nombre, apellido, dni, carrera_id, fechaNacimiento, confirmado) VALUES
                                            ('test@unlam.edu.ar', 'test', 'ADMIN', true, 'Juan', 'Perez',38065944, 1, '1990-01-01', true),
                                            ('fran@unlam.edu.ar', '123', 'ADMIN', true, 'Franco', 'Vargas',41062869, 1, '1998-04-24', true),
                                            ('nat@unlam.edu.ar', '123', 'ADMIN', true, 'Nat', 'alia',41123869, 1, '1998-08-29', true),
                                            ('ro@unlam.edu.ar', '1234', 'ADMIN', true, 'Ro', 'Campa',37659747, 1, '1993-06-18', true);

INSERT INTO Usuario(email, password, rol, activo, nombre, apellido, dni, carrera_id, fechaNacimiento, confirmado)
                VALUES ('admin@unlam.edu.ar', 'admin', 'USER', true, 'Admin', 'Unlam', 32912293, 3, '1990-01-01', true);

-- 5️ Validar que no haya usuarios duplicados
SELECT email, COUNT(*) FROM Usuario GROUP BY email HAVING COUNT(*) > 1;

-- Géneros
INSERT INTO genero (nombre) VALUES ('Femenino'), ('Masculino'), ('Otro'), ('Prefiero no decirlo');

ALTER TABLE Publicacion
CONVERT TO CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id)
VALUES ('Publicación vieja de prueba', '2025-09-27 10:00:00', 3);
UPDATE Usuario
SET ultima_publicacion = '2025-09-27'
WHERE id = 3;

-- 6️Generar slug para los nuevos usuarios
UPDATE Usuario
SET slug = LOWER(CONCAT(nombre, '-', apellido, '-', LPAD(id, 4, '0')))
WHERE slug IS NULL OR slug = '';

-- probar interaccion--
--- 1) Crear publicaciones nuevas
 INSERT INTO Publicacion(descripcion, fechaPublicacion, usuario_id) VALUES
 ('Explorando la programación avanzada', '2025-10-01 09:00:00', 1),
 ('Mis apuntes de matemática I', '2025-10-02 14:30:00', 2),
 ('Economía general: conceptos clave', '2025-10-03 11:15:00', 4),
 ('Proyecto final de desarrollo web', '2025-10-04 16:45:00', 1),
 ('Inteligencia artificial aplicada a la cocina', '2025-10-08 09:00:00', 1),
 ('Cómo diseñar experiencias gastronómicas inmersivas', '2025-10-08 17:20:00', 2),
 ('El futuro de la sostenibilidad alimentaria', '2025-10-09 13:15:00', 4);
INSERT INTO Likes(usuario_id, publicacion_id, fechaDelLike) VALUES
(3, 1, NOW()),
(3, 2, NOW()),
(3, 4, NOW());
 -- 2) Verificar IDs asignados
 SELECT id, descripcion FROM Publicacion;

 -- 3) Registrar like de Nat (id = 3) en la primera publicación
 INSERT INTO Interaccion(usuario_id, publicacion_id, tipo, fecha, peso, vista) VALUES
 (3, 1, 'LIKE', NOW(), 1.0, false);

 -- 4) Nat da like en otra publicación
 INSERT INTO Interaccion(usuario_id, publicacion_id, tipo, fecha, peso, vista) VALUES
 (3, 4, 'LIKE', NOW(), 1.0, false);

 -- 5) Validar
 SELECT * FROM Interaccion WHERE usuario_id = 3;