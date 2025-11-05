package com.tallerwebi.infraestructura;


import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.DatosComentario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ServicioComentarioImpl implements ServicioComentario {

    private final RepositorioComentario repositorioComentario;
    private final ServicioInteraccion servicioInteraccion;
    private final GeminiAnalysisService geminiAnalysisService;

    public ServicioComentarioImpl(RepositorioComentario repositorioComentario,
                                  ServicioInteraccion servicioInteraccion, GeminiAnalysisService geminiAnalysisService) {
        this.repositorioComentario = repositorioComentario;
        this.servicioInteraccion = servicioInteraccion;
        this.geminiAnalysisService = geminiAnalysisService;
    }
    @Override
    public Comentario comentar(DatosComentario dto, Usuario usuario, Publicacion p) {
        Comentario comentario = new Comentario();
        comentario.setTexto(dto.getTexto());
        comentario.setUsuario(usuario);
        comentario.setPublicacion(p);

        // Crear la interaccion
        Interaccion interaccion = new Interaccion();
        interaccion.setUsuario(usuario);
        interaccion.setPublicacion(p);
        interaccion.setTipo("COMENTARIO");
        if (p.getDescripcion() != null) {
            interaccion.setContenido(p.getDescripcion());
        } else {
            // Si no tiene descripción, al menos guarda una etiqueta para que no sea NULL
            interaccion.setContenido("[COMENTARIO a publicación sin texto]");
        }
        interaccion.setFecha(LocalDateTime.now());
        interaccion.setPeso(1.0); // o lo que uses para el peso
        interaccion.setVista(false);
        servicioInteraccion.guardarInteraccion(interaccion);
        geminiAnalysisService.analizarYGuardarGustos(usuario);


        return repositorioComentario.guardar(comentario);
    }

    @Override
    public void editarComentario(Comentario comentario) {

    }

    @Override
    public int contarComentarios(long publiId) {
        return repositorioComentario.contarComentarioPorPublicacion(publiId);


    }

    @Override
    public List<Comentario> encontrarComentariosPorId(long id) {
        return repositorioComentario.findComentariosByPublicacionId(id);
    }

    @Override
    public Usuario usuarioqueComento(long usuId) {
        return repositorioComentario.encontrarUsuarioQueHizoComentario(usuId);
    }

}