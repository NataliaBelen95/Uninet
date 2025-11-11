package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.DTO.DatosPublicacion;
import com.tallerwebi.presentacion.DTO.PublicacionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioFeed {

    private final ServicioPublicacion servicioPublicacion;
    private final ServicioRecomendaciones servicioRecomendaciones;
    private final PublicacionMapper publicacionMapper;

    @Autowired
    public ServicioFeed(ServicioPublicacion servicioPublicacion,
                        ServicioRecomendaciones servicioRecomendaciones,
                        PublicacionMapper publicacionMapper) {
        this.servicioPublicacion = servicioPublicacion;
        this.servicioRecomendaciones = servicioRecomendaciones;
        this.publicacionMapper = publicacionMapper;
    }

    public List<DatosPublicacion> obtenerFeedPrincipal(long usuarioId) {
        List<Publicacion> publicacionesOrganicas = servicioPublicacion.obtenerTodasPublicacionesIgnorandoPublicidades();
        return publicacionesOrganicas.stream()
                .map(p -> publicacionMapper.toDto(p, usuarioId)) // Usa el mapper y el ID
                .collect(Collectors.toList());
    }

    public List<DatosPublicacion> obtenerFeedRecomendado(Usuario usuario, Long idUsuario) throws Exception {
        List<Publicacion> publisRecomendadas = servicioRecomendaciones.recomendarParaUsuario(usuario, 5);
        List<Publicacion> publisBots = servicioPublicacion.obtenerPublisBotsParaUsuario(usuario);

        List<DatosPublicacion> resultado = new ArrayList<>();
        resultado.addAll(
                publisRecomendadas.stream()
                        .map(p -> publicacionMapper.toDto(p, idUsuario))
                        .collect(Collectors.toList())
        );

        resultado.addAll(
                publisBots.stream()
                        .map(p -> publicacionMapper.toDto(p, idUsuario))
                        .collect(Collectors.toList())
        );
        System.out.println("[FeedRecomendado] Total publicaciones combinadas: " + resultado.size());

        return resultado;

    }

}