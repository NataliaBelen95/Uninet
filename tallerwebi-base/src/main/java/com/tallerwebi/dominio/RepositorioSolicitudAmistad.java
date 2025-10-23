package com.tallerwebi.dominio;

import com.tallerwebi.dominio.SolicitudAmistad;
import com.tallerwebi.dominio.Usuario;
import java.util.List;

public interface RepositorioSolicitudAmistad {
    void guardar(SolicitudAmistad solicitud);
    SolicitudAmistad buscarPorId(Long id);
    List<SolicitudAmistad> buscarPendientes(Usuario usuario);
    List<Usuario> buscarAmigos(Usuario usuario);
    void actualizar(SolicitudAmistad solicitud);
}
