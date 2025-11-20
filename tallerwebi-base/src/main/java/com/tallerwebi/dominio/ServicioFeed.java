package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.DTO.DatosPublicacion;
import com.tallerwebi.presentacion.DTO.PublicacionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        List<Publicacion> publicacionesDeAmigos = servicioPublicacion.publicacionesDeAmigos(usuarioId);

        // 2. Mapear y devolver
        return publicacionesDeAmigos.stream()
                .map(p -> publicacionMapper.toDto(p, usuarioId))
                .collect(Collectors.toList());
    }

    public List<DatosPublicacion> obtenerFeedRecomendado(Usuario usuario, Long idUsuario) throws Exception {
        List<Publicacion> publisRecomendadas = servicioRecomendaciones.recomendarParaUsuario(usuario, 5);
        List<Publicacion> publisBots = servicioPublicacion.obtenerPublisBotsParaUsuario(usuario);
        List<Publicacion> publicacionesUsuario = servicioPublicacion.obtenerPublicacionesDeUsuario(idUsuario);

        // Creamos la lista combinada (ArrayList para permitir la mutación y la adición)
        List<DatosPublicacion> resultado = new ArrayList<>();

        // 1. Convertir y agregar publicaciones recomendadas
        resultado.addAll(
                publisRecomendadas.stream()
                        .map(p -> publicacionMapper.toDto(p, idUsuario))
                        .collect(Collectors.toList())
        );

        // 2. Convertir y agregar publicaciones del bot
        resultado.addAll(
                publisBots.stream()
                        .map(p -> publicacionMapper.toDto(p, idUsuario))
                        .collect(Collectors.toList())
        );

        // 3. Convertir y agregar publicaciones del propio usuario
        resultado.addAll(
                publicacionesUsuario.stream()
                        .map(p -> publicacionMapper.toDto(p, idUsuario))
                        .collect(Collectors.toList())
        );


        Collections.sort(resultado, new Comparator<DatosPublicacion>() {
            @Override
            public int compare(DatosPublicacion p1, DatosPublicacion p2) {
                // Manejar nulos para las publicaciones
                if (p1.getFechaPublicacion() == null && p2.getFechaPublicacion() == null) {
                    return 0; // Son iguales si ambos son nulos
                }
                if (p1.getFechaPublicacion() == null) {
                    return 1; // Poner nulos al final (p1 es 'mayor' que p2)
                }
                if (p2.getFechaPublicacion() == null) {
                    return -1; // Poner nulos al final (p1 es 'menor' que p2)
                }

                // Comparar de forma DESCENDENTE (más reciente primero)
                // p2.compareTo(p1) => Orden Descendente
                return p2.getFechaPublicacion().compareTo(p1.getFechaPublicacion());
            }
        });

        System.out.println("[FeedRecomendado] Total publicaciones combinadas: " + resultado.size());

        return resultado;

    }

}