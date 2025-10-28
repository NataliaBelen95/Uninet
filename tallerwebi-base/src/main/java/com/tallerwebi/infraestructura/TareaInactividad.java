package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.ServicioInactividad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;

@EnableScheduling
@Service
public class TareaInactividad {
       @Autowired
        private ServicioInactividad servicioInactividad;

        // Se ejecuta todos los días a las 9am
        @Scheduled(cron = "0 0 9 * * ?")
        public void ejecutarNotificaciones() {
            servicioInactividad.notificarUsuariosInactivos(0); // fuerza la notificación para todos
        }
}
