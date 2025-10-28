package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ServicioInactividad {
  /*Busca todos los usuarios que no han publicado en los últimos 'diasInactividad' días.*/
    List<Usuario> obtenerUsuariosInactivos (int diasInactividad);

    void notificarUsuariosInactivos(int diasInactividad);


}
