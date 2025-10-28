package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ServicioInactividadImpl implements ServicioInactividad {


    @Autowired
    private RepositorioUsuario repositorioUsuario; // tu repo de usuarios

    @Autowired
    private ServicioNotificacion servicioNotificacion;

    /**
     * Obtiene usuarios que no han publicado en los últimos 'diasInactividad' días
     */
    @Override
    public List<Usuario> obtenerUsuariosInactivos(int diasInactividad) {
        LocalDate fechaLimite = LocalDate.now().minusDays(diasInactividad);
        return repositorioUsuario.buscarUsuariosInactivosPorFechaUltimaPublicacionOSinPublicacion(fechaLimite);
    }

    /**
     * Genera notificaciones de inactividad para los usuarios inactivos
     */
    @Override
    public void notificarUsuariosInactivos(int diasInactividad) {
        List<Usuario> inactivos = obtenerUsuariosInactivos(diasInactividad);

        for (Usuario usuario : inactivos) {
            // emisor puede ser null o el "sistema"
            servicioNotificacion.crear(usuario, null, null, TipoNotificacion.INACTIVIDAD);
        }
    }
}
