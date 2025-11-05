package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionNoEncontrada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicioLikeImpl implements ServicioLike {
    private final RepositorioLike repositorioLike;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioPublicacion repositorioPublicacion;
    private final ServicioInteraccion servicioInteraccion;
    private final GeminiAnalysisService geminiAnalysisService;

    @Autowired
    public ServicioLikeImpl(RepositorioLike repositorioLike,RepositorioUsuario repositorioUsuario,
                            RepositorioPublicacion repositorioPublicacion,
                            ServicioInteraccion servicioInteraccion, GeminiAnalysisService geminiAnalysisService) {
        this.repositorioLike = repositorioLike;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioPublicacion = repositorioPublicacion;
        this.servicioInteraccion = servicioInteraccion;
        this.geminiAnalysisService = geminiAnalysisService;


    }


    @Override
    public void darLike(long usuId, long publiId) {
        Like like = new Like();

        // Buscar el usuario y la publicación por ID
        Usuario usuario = repositorioUsuario.buscarPorId(usuId); // Busca al usuario por ID
        Publicacion publicacion = repositorioPublicacion.buscarPorId(publiId); // Busca la publicación por ID

        // Asignar los objetos completos a Like
        like.setUsuario(usuario); // Asignar el objeto Usuario
        like.setPublicacion(publicacion); // Asignar el objeto Publicacion
        like.setFecha(LocalDateTime.now()); // Asignar la fecha del like

        // Guardar el like en el repositorio
        repositorioLike.guardar(like);
    }


    @Override
    public void quitarLike(long id) {
        Like like = repositorioLike.buscarPorId(id);
        if (like != null) {
            repositorioLike.eliminar(like.getId());
        }
    }

    @Override
    public boolean yaDioLike(long usuId, long publiId) {
        return repositorioLike.existePorUsuarioYPublicacion(usuId, publiId);
    }
    //HACER TOGGLE
    @Override
    public void toggleLike(long idUsuario, long publiId) {
            Publicacion publicacion = repositorioPublicacion.buscarPorId(publiId);
            if(publicacion == null) {
                throw new PublicacionNoEncontrada("Error al encontrar publicacion que se quiere dar like");
            }
            Usuario usuario = repositorioUsuario.buscarPorId(idUsuario);

            boolean yaDioLike = repositorioLike.existePorUsuarioYPublicacion(idUsuario, publiId);
            if (yaDioLike) {
                Like like = repositorioLike.buscarPorUsuarioYPublicacion(idUsuario, publiId);
                if(like != null) {
                    repositorioLike.eliminar(like.getId());
                }
                // Eliminar la interaccion asociada
                List<Interaccion> interacciones = servicioInteraccion.obtenerInteraccionesDeUsuario(usuario)
                        .stream()
                        .filter(i -> i.getPublicacion().getId() == publiId && "LIKE".equals(i.getTipo()))
                        .collect(Collectors.toList());
                for (Interaccion inter : interacciones) {
                    servicioInteraccion.eliminarInteraccion(inter.getId());
                }
            }   else {
                Like like = new Like();
                like.setUsuario(usuario);
                like.setPublicacion(publicacion);
                like.setFecha(LocalDateTime.now());
                repositorioLike.guardar(like);

                // Crear la interaccion
                Interaccion interaccion = new Interaccion();
                interaccion.setUsuario(usuario);
                interaccion.setPublicacion(publicacion);
                interaccion.setTipo("LIKE");
                // Copiar la descripción de la Publicación al contenido de la Interaccion
                if (publicacion.getDescripcion() != null) {
                    interaccion.setContenido(publicacion.getDescripcion());
                } else {
                    // Si no tiene descripción, al menos guarda una etiqueta para que no sea NULL
                    interaccion.setContenido("[LIKE a publicación sin texto]");
                }
                interaccion.setFecha(LocalDateTime.now());
                interaccion.setPeso(1.0); // o lo que uses para el peso
                interaccion.setVista(false);
                servicioInteraccion.guardarInteraccion(interaccion);
                geminiAnalysisService.analizarYGuardarGustos(usuario);
            }
    }

    @Override
    public int contarLikes(long publiId) {
        return repositorioLike.contarPorPublicacion(publiId);
    }

    @Override
    public Like obtenerLike(long usuId, long publiId) {
        return repositorioLike.buscarPorUsuarioYPublicacion(usuId, publiId);
    }
}
