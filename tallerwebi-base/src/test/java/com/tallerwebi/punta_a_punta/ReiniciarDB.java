package com.tallerwebi.punta_a_punta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReiniciarDB {
    public static void limpiarBaseDeDatos() {
        try {
            String dbHost = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
            String dbPort = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
            String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "tallerwebi";
            String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "user";
            String dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "user";
            //windows
            // La limpieza está casi perfecta. Se añade 'confirmado' al INSERT.
            String sqlCommands =
                    // 1. Deshabilitar la verificación
                    "SET FOREIGN_KEY_CHECKS = 0;\n" +

                            // 2. Limpieza de Tablas HIJAS/NIETAS
                            "DELETE FROM Comentario;\n" +
                            "DELETE FROM Likes;\n" +
                            "DELETE FROM ChatMessage;\n" +
                            "DELETE FROM Publicacion;\n" +
                            "DELETE FROM Notificacion;\n" +
                            "DELETE FROM GustosPersonal;\n" +

                            // 3. Limpieza de Tablas PADRE (y dependencias)
                            "DELETE FROM Usuario;\n" +
                            "DELETE FROM Departamento;\n" +
                            "DELETE FROM Carrera;\n" +

                            // 4. Inserción de DEPENDENCIAS (Carrera y Departamento)
                            "INSERT INTO Departamento(id, nombre) VALUES(1, 'Informática');\n" +
                            // CORRECCIÓN CLAVE: ASOCIAR la carrera ID=1 al departamento ID=1
                            "INSERT INTO Carrera(id, nombre, departamento_id) VALUES(1, 'Tecnicatura en Programación', 1);\n" +

                            // 5. Reinsertar el usuario de prueba con TODOS los campos NOT NULL y el campo 'confirmado'
                            "ALTER TABLE Usuario AUTO_INCREMENT = 1;\n" +

                            "INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, dni, esBot, departamento_id, carrera_id, confirmado) " +
                            "VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true, 'Test', 'User', 12345678, false, 1, 1, false);\n" +

                            // 6. Rehabilitar la verificación
                            "SET FOREIGN_KEY_CHECKS = 1;";
//            String sqlCommands = "DELETE FROM Usuario;\n" +
//                    "ALTER TABLE Usuario AUTO_INCREMENT = 1;\n" +
//                    "INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true);";
//
//            String comando = String.format(
//                    "docker exec tallerwebi-mysql mysql -h %s -P %s -u %s -p%s %s -e \"%s\"",
//                    dbHost, dbPort, dbUser, dbPassword, dbName, sqlCommands
//            );
//
//            Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", comando});
//            int exitCode = process.waitFor();
//
//            if (exitCode == 0) {
//                System.out.println("Base de datos limpiada exitosamente");
//            } else {
//                System.err.println("Error al limpiar la base de datos. Exit code: " + exitCode);
//            }
            String[] comando = new String[]{
                    "docker",
                    "exec",
                    "mysql-container", // El nombre correcto de tu contenedor Docker
                    "mysql",
                    "-h", dbHost,
                    "-P", dbPort,
                    "-u", dbUser,
                    String.format("-p%s", dbPassword),
                    dbName,
                    "-e",
                    sqlCommands
            };

            // Ahora se ejecuta el array de argumentos directamente.
            Process process = Runtime.getRuntime().exec(comando);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Base de datos limpiada exitosamente");
            } else {
                java.io.InputStream errorStream = process.getErrorStream();
                String error = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                System.err.println("Error al limpiar la base de datos. Exit code: " + exitCode + ". Error: " + error);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error ejecutando script de limpieza: " + e.getMessage());
            e.printStackTrace();
        }
    }
    }

